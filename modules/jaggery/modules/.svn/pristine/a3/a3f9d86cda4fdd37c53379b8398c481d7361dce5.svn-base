var registry = registry || {};

(function (server, registry) {
    var log = new Log();

    var Resource = Packages.org.wso2.carbon.registry.core.Resource;

    var Collection = Packages.org.wso2.carbon.registry.core.Collection;

    var Comment = Packages.org.wso2.carbon.registry.core.Comment;

    var StaticConfiguration = Packages.org.wso2.carbon.registry.core.config.StaticConfiguration;

    var queryPath = '/_system/config/repository/components/org.wso2.carbon.registry/queries/';

    var content = function (registry, resource, paging) {
        if (resource instanceof Collection) {
            // #1 : this always sort children by name, so sorting cannot be done for the chunk
            return function (pagination) {
                pagination = pagination || paging;
                return children(registry, resource, pagination);
            };
        }
        if (resource instanceof Comment) {
            return String(resource.getText());
        }
        var stream = resource.getContentStream();
        if (stream) {
            return new Stream(stream);
        }
        return String(resource.content);
    };

    var resourceSorter = function (key) {
        var nameAsc = function (l, r) {
            var lname, rname;
            if (l instanceof Collection) {
                lname = l.getName();
                lname = lname.substring(lname.lastIndexOf('/') + 1);
            } else {
                lname = l.name;
            }
            if (r instanceof Collection) {
                rname = r.getName();
                rname = rname.substring(rname.lastIndexOf('/') + 1);
            } else {
                rname = r.name;
            }
            return lname === rname ? 0 : (lname > rname ? 1 : -1);
        };
        switch (key) {
            case 'time-created-asc' :
                return function (l, r) {
                    return l.getCreatedTime().getTime() - r.getCreatedTime().getTime();
                };
            case 'time-created-des' :
                return function (l, r) {
                    return r.getCreatedTime().getTime() - l.getCreatedTime().getTime();
                };
            case 'name-asc' :
                return nameAsc;
            case 'name-des' :
                return function (l, r) {
                    return -nameAsc(l, r);
                };
            default:
                return resourceSorter('time-created-des');
        }
    };

    var children = function (registry, resource, paging) {
        var length, i, resources,
            paths = [];
        resources = registry.content(resource.path);
        //we have to manually sort this due to the bug in registry.getChildren() (#1 above)
        resources.sort(resourceSorter(paging.sort));
        resources = resources.slice(paging.start, paging.start + paging.count);
        length = resource.length;
        for (i = 0; i < length; i++) {
            paths.push(resources[i].path);
        }
        return paths;
    };

    var resource = function (registry, resource) {
        var path = String(resource.path),
            o = {
                created: {
                    author: String(resource.authorUserName),
                    time: resource.createdTime.time
                },
                content: content(registry, resource, {
                    start: 0,
                    count: 10
                }),
                id: String(resource.id),
                version: resource.versionNumber
            };
        if (resource instanceof Comment) {
            return o;
        }
        if (resource instanceof Collection) {
            o.collection = (resource instanceof Collection);
        }
        o.uuid = String(resource.UUID);
        o.path = String(path);
        o.name = String(resource.name) || resolveName(path);
        o.description = String(resource.description);
        o.updated = {
            author: String(resource.lastUpdaterUserName),
            time: resource.lastModified.time
        };
        o.mediaType = String(resource.mediaType);
        o.properties = function () {
            return properties(resource);
        };
        o.aspects = function () {
            return aspects(resource);
        };
        return o;
    };

    var properties = function (resource) {
        var prop,
            properties = resource.properties,
            props = properties.keySet().toArray(),
            length = props.length,
            o = {};
        for (var i = 0; i < length; i++) {
            prop = props[i];
            o[prop] = resource.getPropertyValues(prop).toArray();
        }
        return o;
    };

    var aspects = function (resource) {
        var aspects = resource.getAspects();
        return aspects ? aspects.toArray() : [];
    };

    var resolveName = function (path) {
        path = path.charAt(path.length - 1) === '/' ? path.substring(0, path.length - 1) : path;
        return path.substring(path.lastIndexOf('/') + 1);
    };

    var merge = function (def, options) {
        if (options) {
            for (var op in def) {
                if (def.hasOwnProperty(op)) {
                    def[op] = options[op] || def[op];
                }
            }
        }
        return def;
    };

    var Registry = function (serv, options) {
        var registryService = server.osgiService('org.wso2.carbon.registry.core.service.RegistryService'),
            carbon = require('carbon');
        if (options) {
            this.server = serv;
        } else {
            this.server = new server.Server();
            options = serv || {};
        }

        if (options.tenantId) {
            this.tenantId = options.tenantId;
        } else if (options.username || options.domain) {
            this.tenantId = server.tenantId({
                domain: options.domain,
                username: options.username
            });
        } else {
            this.tenantId = server.tenantId();
        }

        if (options.username) {
            this.username = options.username;
        } else if (options.system) {
            this.username = carbon.user.systemUser;
        } else {
            this.username = carbon.user.anonUser;
        }

        this.registry = registryService.getRegistry(this.username, this.tenantId);
        this.versioning = {
            comments: StaticConfiguration.isVersioningComments()
        };
    };

    registry.Registry = Registry;

    Registry.prototype.put = function (path, resource) {
        var res;
        if (resource.collection) {
            res = this.registry.newCollection();
        } else {
            res = this.registry.newResource();
            if (resource.content instanceof Stream) {
                res.contentStream = resource.content.getStream();
            } else {
                res.content = resource.content || null;
            }
            res.mediaType = resource.mediaType || null;
        }
        res.name = resource.name;
        res.description = resource.description || null;
        res.UUID = resource.uuid || null;

        var values, length, i, ArrayList,
            properties = resource.properties;
        if (properties) {
            ArrayList = java.util.ArrayList;
            for (var name in properties) {
                var list = new ArrayList();
                if (properties.hasOwnProperty(name)) {
                    values = properties[name];
                    values = values instanceof Array ? values : [values];
                    length = values.length;
                    for (i = 0; i < length; i++) {
                        list.add(values[i]);
                    }
                    res.setProperty(name, list);
                }
            }
        }

        var aspects = resource.aspects;
        if (aspects) {
            length = aspects.length;
            for (i = 0; i < length; i++) {
                res.addAspect(aspects[i]);
            }
        }

        this.registry.put(path, res);
    };

    Registry.prototype.remove = function (path) {
        this.registry.delete(path);
    };

    Registry.prototype.move = function (src, dest) {
        this.registry.move(src, dest);
    };

    Registry.prototype.rename = function (current, newer) {
        this.registry.rename(current, newer);
    };

    Registry.prototype.copy = function (src, dest) {
        this.registry.rename(src, dest);
    };

    Registry.prototype.restore = function (path) {
        this.registry.restoreVersion(path);
    };

    Registry.prototype.get = function (path) {
        if (!this.exists(path)) {
            return null;
        }
        var res = this.registry.get(path);
        return resource(this, res);
    };

    Registry.prototype.exists = function (path) {
        return this.registry.resourceExists(path);
    };

    Registry.prototype.content = function (path, paging) {
        if (!this.exists(path)) {
            return null;
        }
        var resource = this.registry.get(path);
        paging = merge({
            start: 0,
            count: 10,
            sort: 'recent'
        }, paging);
        return content(this, resource, paging);
    };

    Registry.prototype.tags = function (path) {
        var tags, i, length,
            tagz = [];
        tags = this.registry.getTags(path);
        length = tags.length;
        for (i = 0; i < length; i++) {
            tagz.push(String(tags[i].tagName));
        }
        return tagz;
    };

    Registry.prototype.tag = function (path, tags) {
        var i, length;
        tags = tags instanceof Array ? tags : [tags];
        length = tags.length;
        for (i = 0; i < length; i++) {
            this.registry.applyTag(path, tags[i]);
        }
    };

    Registry.prototype.untag = function (path, tags) {
        var i, length;
        tags = tags instanceof Array ? tags : [tags];
        length = tags.length;
        for (i = 0; i < length; i++) {
            this.registry.removeTag(path, tags[i]);
        }
    };

    Registry.prototype.associate = function (src, dest, type) {
        this.registry.addAssociation(src, dest, type);
    };

    Registry.prototype.dissociate = function (src, dest, type) {
        this.registry.removeAssociation(src, dest, type);
    };

    Registry.prototype.associations = function (path, type) {
        var i, asso,
            assos = type ? this.registry.getAssociations(path, type) : this.registry.getAllAssociations(path),
            length = assos.length(),
            associations = [];
        for (i = 0; i < length; i++) {
            asso = assos[i];
            associations.push({
                type: String(asso.associationType),
                src: String(asso.sourcePath),
                dest: String(asso.destinationPath)
            });
        }
        return associations;
    };

    Registry.prototype.addProperty = function (path, name, value) {
        var resource = this.registry.get(path);
        resource.addProperty(name, value);
    };

    Registry.prototype.removeProperty = function (path, name, value) {
        var resource = this.registry.get(path);
        (value ? resource.removePropertyValue(name, value) : resource.removeProperty(name));
    };

    Registry.prototype.properties = function (path) {
        var resource = this.registry.get(path);
        return properties(resource);
    };

    Registry.prototype.version = function (path) {
        this.registry.createVersion(path);
    };

    Registry.prototype.versions = function (path) {
        return this.registry.getVersions(path);
    };

    Registry.prototype.unversion = function (path, snapshot) {
        this.registry.removeVersionHistory(path, snapshot);
    };

    Registry.prototype.comment = function (path, comment) {
        this.registry.addComment(path, new Comment(comment));
    };

    Registry.prototype.comments = function (path, paging) {
        var i, length, comments, comment, key,
            commentz = [];
        paging = merge({
            start: 0,
            count: 25,
            sort: 'recent'
        }, paging);

        comments = this.registry.getComments(path);
        //we have to manually sort this due to the bug in registry.getChildren() (#1 above)
        key = paging.sort;
        key = (key === 'time-created-des' || key === 'time-created-asc') ? key : 'time-created-des';
        comments = comments.sort(resourceSorter(key));
        comments = comments.slice(paging.start, paging.start + paging.count);
        length = comments.length;
        for (i = 0; i < length; i++) {
            comment = comments[i];
            commentz.push({
                content: comment.getText(),
                created: {
                    author: comment.getUser(),
                    time: comment.getCreatedTime().getTime()
                },
                path: comment.getCommentPath()
            });
        }
        return commentz;
    };

    Registry.prototype.commentCount = function (path) {
        return this.registry.getComments(path).length;
    };

    Registry.prototype.uncomment = function (path) {
        this.registry.removeComment(path);
    };

    Registry.prototype.rate = function (path, rating) {
        this.registry.rateResource(path, rating);
    };

    Registry.prototype.unrate = function (path) {
        this.registry.rateResource(path, 0);
    };

    Registry.prototype.rating = function (path, username) {
        var rating = {
            average: this.registry.getAverageRating(path)
        };
        if (username) {
            rating.user = this.registry.getRating(path, username);
        }
        return rating;
    };

    Registry.prototype.link = function (path, target) {
        return this.registry.createLink(path, target);
    };

    Registry.prototype.unlink = function (path) {
        return this.registry.removeLink(path);
    };

    Registry.prototype.search = function (query, paging) {
        var res = this.registry.searchContent(query);
        paging = merge({
            start: 0,
            count: 10,
            sort: 'recent'
        }, paging);
        return res ? content(this, res, paging) : [];
    };

    Registry.prototype.query = function (path, params) {
        var res, name,
            map = new java.util.HashMap();
        for (name in params) {
            if (params.hasOwnProperty(name)) {
                map.put(name, params[name]);
            }
        }
        res = this.registry.executeQuery(path, map);
        return res.getChildren();
    };

}(server, registry));