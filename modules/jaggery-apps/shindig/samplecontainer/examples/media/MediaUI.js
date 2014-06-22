/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

/*
 * The User Interface for the Albums & MediaItems gadget.
 *
 * SHINDIG TODOS
 *  set ownerId automatically?
 *  delete children mediaitems when album deleted?
 *  update only updates given fields?
 *  update album mediaitem count when inserting/removing mediaitem?
 *
 * GADGET TODOS
 *  album info such as how many albums are contained
 *  fix auto height for edit album popup
 *  thumnail pictures
 */
function MediaUI(social) {
    var viewer = null;
    var divManager = null;

    var folderUrl = 'http://www.clker.com/cliparts/2/b/b/3/1194983972976950993blue_folder_seth_yastrov_01.svg.med.png';
    var docUrl = 'http://www.plastyc.com/images/document-icon.png';

    /*
     * Initializes the gadget.
     */
    this.init = function() {
        console.log('initializing AlbumsUI');

        // Manages high-level divs
        divManager = new DivManager();
        divManager.init();

        // Load data and render
        loadData(function() {
            social.getAlbumsByUser(viewer.id, function(response) {
                renderAlbums(response.list);
                divManager.showAlbums();
            });
        });
    }

    /*
     * Pre-load data for gadget.
     */
    function loadData(callback) {
        social.getViewer(function(data) {
            viewer = data;
            callback();
        });
    }

    /*
     * Manages the gadgets main DIV elements.
     *
     * TODO: use dojo.query() & classes rather than divs[]
     * TODO: showOnly() function to avoid flashing/pauses
     */
    function DivManager() {
        var divs = [];

        this.init = function() {
            console.log('DivManager.init');
            addDiv('albumsDiv');
            addDiv('mediaItemsDiv');
            addDiv('mediaItemDiv');
            hideAll();
        }

        this.showAlbums = function() {
            console.log('DivManager.showAlbums');
            hideAll();
            divs['albumsDiv'].style.display = 'block';
            this.refreshWindow();
        }

        this.showMediaItems = function() {
            console.log('DivManager.showMediaItems');
            hideAll();
            divs['mediaItemsDiv'].style.display = 'block';
            this.refreshWindow();
        }

        this.showMediaItem = function() {
            console.log('DivManager.showMediaItem');
            hideAll();
            divs['mediaItemDiv'].style.display = 'block';
            this.refreshWindow();
        }

        this.refreshWindow = function() {
            gadgets.window.adjustHeight(500);
        }

        function hideAll() {
            for (key in divs) { divs[key].style.display = 'none'; }
        }

        function addDiv(id) { divs[id] = dojo.create('div', {id: id}, dojo.body()); }
    }

    /*
     * Renders a list of the given albums.
     */
    function renderAlbums(albums) {
        console.log('renderAlbums');

        dojo.empty('albumsDiv');
        var albumsDiv = dojo.byId('albumsDiv');

        var albumsBanner = dojo.create('div', null, albumsDiv);
        var table = dojo.create('table', null, albumsBanner);
        var tbody = dojo.create('tbody', null, table);
        var tr = dojo.create('tr', null, tbody);
        dojo.create('td', {innerHTML: viewer.name.formatted + "'s Albums", className: 'albumsTitle'}, tr);
        dojo.create('td', null, tr).appendChild(new dijit.form.Button({label: '+ New Album', onClick: dojo.hitch(this, editAlbumPopup, null)}).domNode);

        var albumsList = dojo.create('div', null, albumsDiv);
        if (albums.length > 0) {
            var table = dojo.create('table', {className: 'albumsTable'}, albumsList);
            var tbody = dojo.create('tbody', null, table);
            for (i = 0; i < albums.length; i++) {
                var albumRow = dojo.create('tr', null, tbody);
                var albumLeft = dojo.create('td', {className: 'albumListThumbnail'}, albumRow);
                var imgLink = dojo.create('a', {href: 'javascript:;', onclick: dojo.hitch(this, onClickAlbum, viewer.id, albums[i])}, albumLeft);
                dojo.create('img', {src: albums[i].thumbnailUrl || folderUrl, onerror: "this.src='" + folderUrl + "';", width: '100', height:'100'}, imgLink);
                var albumRight = dojo.create('td', {className: 'albumListRight'}, albumRow);
                var albumTitleTbody = dojo.create('table', null, albumRight).appendChild(dojo.create('tbody', null));
                var albumTitleRow = dojo.create('tr', null, albumTitleTbody);
                var titleTd = dojo.create('td', {className: 'albumListTitle'}, albumTitleRow);
                dojo.create('a', {innerHTML: albums[i].title, href: 'javascript:;', onclick: dojo.hitch(this, onClickAlbum, viewer.id, albums[i])}, titleTd);
                var editTd = dojo.create('td', {className: 'actionLinks'}, albumTitleRow);
                editTd.style.textAlign="right";
                dojo.create('a', {innerHTML: 'edit', href: 'javascript:;', onclick: dojo.hitch(this, editAlbumPopup, albums[i])}, editTd);
                editTd.appendChild(dojo.doc.createTextNode(' | '));
                dojo.create('a', {innerHTML: 'delete', href: 'javascript:;', onclick: dojo.hitch(this, deleteAlbumPopup, albums[i])}, editTd);
                if (albums[i].description) {
                    var albumDescription = dojo.create('tr', null, albumTitleTbody);
                    dojo.create('td', {innerHTML: albums[i].description, className: 'albumListDescription', colSpan: '2'}, albumDescription);
                }
                //var albumInfo = dojo.create('tr', null, albumRight);
                //var infoStr = "ID: " + albums[i].id + " | Owner ID: " + albums[i].ownerId;
                //dojo.create('td', {innerHTML: infoStr, className: 'albumListInfo', colspan: '2'}, albumInfo);
            }
        } else {
            albumsDiv.appendChild(dojo.doc.createTextNode('No albums found.'));
        }
        divManager.refreshWindow();

        // Handles when user clicks an album
        function onClickAlbum(userId, album) {
            social.getMediaItemsByAlbum(userId, album.id, function(response) {
                renderMediaItems(album, response.list);
                divManager.showMediaItems();
            });
        }
    }

    /*
     * Convenience function to retrieve albums and render.
     */
    function renderAlbumsByUser(userId, callback) {
        social.getAlbumsByUser(userId, function(response) {
            renderAlbums(response.list);
            divManager.showAlbums();
            if (callback != null) callback();
        });
    }

    /*
     * Renders a grid of the given MediaItems.
     *
     * TODO: simplify this by simply taking in 'album', retrieving MediaItems here
     */
    function renderMediaItems(album, mediaItems) {
        console.log('renderMediaItems');
        dojo.empty('mediaItemsDiv');
        var mediaItemsDiv = dojo.byId('mediaItemsDiv');
        var numCols = 5;

        // Div to display navation bar and Create button
        var topDiv = dojo.create('div', null, mediaItemsDiv);
        var table = dojo.create('table', null, topDiv);
        var tbody = dojo.create('tbody', null, table);
        var tr = dojo.create('tr', null, tbody);
        var td = dojo.create('td', {style: 'width:100%'}, tr);
        dojo.create('a', {innerHTML: 'Albums', href: 'javascript:;', onclick: dojo.hitch(this, renderAlbumsByUser, viewer.id, null)}, td);
        td.appendChild(dojo.doc.createTextNode(' > ' + album.title));
        td = dojo.create('td', {style: 'width:100%'}, tr);
        var createButton = new dijit.form.Button({label: '+ New MediaItem', onClick: dojo.hitch(this, editMediaItemPopup, album, null)});
        td.appendChild(createButton.domNode);

        // Div to display MediaItems in a grid
        var gridDiv = dojo.create('div', null, mediaItemsDiv);
        if (mediaItems.length > 0) {
            var table = dojo.create('table', null, gridDiv);
            var tbody = dojo.create('tbody', null, table);
            var tr = null;
            for (i = 0; i < mediaItems.length; i++) {
                if (i % numCols == 0) {
                    tr = dojo.create('tr', null, tbody);
                }
                var td = dojo.create('td', {className: 'mediaItemBox'}, tr);
                var imageTbody = dojo.create('table', null, td).appendChild(dojo.create('tbody', null));
                var imageTd = dojo.create('tr', null, imageTbody).appendChild(dojo.create('td', {className: 'mediaItemThumbnail'}));
                if (mediaItems[i].url) {
                    var imageLink = dojo.create('a', {href: 'javascript:;', onclick: dojo.hitch(this, renderMediaItem, album, mediaItems[i])}, imageTd);
                    imageLink.appendChild(dojo.create('img', {src: mediaItems[i].thumbnailUrl, height: '100', width:'100'}));
                } else {
                    dojo.create('img', {src: mediaItems[i].thumbnailUrl, onerror: "this.src='" + docUrl + "';", height:'100', width:'100'}, imageTd);
                }
                var titleTbody = dojo.create('table', null, td).appendChild(dojo.create('tbody', null));
                var titleTd = dojo.create('tr', null, titleTbody).appendChild(dojo.create('td', {style: "text-align:center; font-family:'comic sans ms';white-space:nowrap;"}));
                titleTd.appendChild(dojo.doc.createTextNode(mediaItems[i].title));
                var actionsTbody = dojo.create('table', null, td).appendChild(dojo.create('tbody', null));
                var actionsTd = dojo.create('tr', null, actionsTbody).appendChild(dojo.create('td', {className: 'actionLinks', style: 'text-align: center;'}));
                dojo.create('a', {innerHTML: 'edit', href: 'javascript:;', onclick: dojo.hitch(this, editMediaItemPopup, album, mediaItems[i])}, actionsTd);
                actionsTd.appendChild(dojo.doc.createTextNode(' | '));
                dojo.create('a', {innerHTML: 'delete', href: 'javascript:;', onclick: dojo.hitch(this, deleteMediaItemPopup, album, mediaItems[i])}, actionsTd);
            }
        } else {
            gridDiv.appendChild(dojo.doc.createTextNode('Album is empty'));
        }
        divManager.refreshWindow();
    }

    /*
     * Convenience function to retriev & render MediaItems by Album.
     */
    function retrieveAndRenderMediaItems(album) {
        social.getMediaItemsByAlbum(viewer.id, album.id, function(response) {
            divManager.showMediaItems();
            renderMediaItems(album, response.list);
        });
    }

    /*
     * Renders the view for a single MediaItem.
     */
    function renderMediaItem(album, mediaItem) {
        console.log('renderMediaItem');
        dojo.empty('mediaItemDiv');
        var mediaItemDiv = dojo.byId('mediaItemDiv');

        // Div to display navation bar and Create button
        var topDiv = dojo.create('div', null, mediaItemDiv);
        var table = dojo.create('table', null, topDiv);
        var tbody = dojo.create('tbody', null, table);
        var tr = dojo.create('tr', null, tbody);
        var td = dojo.create('td', {style: 'width:100%'}, tr);
        dojo.create('a', {innerHTML: 'Albums', href: 'javascript:;', onclick: dojo.hitch(this, renderAlbumsByUser, viewer.id, null)}, td);
        td.appendChild(dojo.doc.createTextNode(' > '));
        dojo.create('a', {innerHTML: album.title, href: 'javascript:;', onclick: dojo.hitch(this, retrieveAndRenderMediaItems, album)}, td);
        td.appendChild(dojo.doc.createTextNode(' > ' + mediaItem.title));

        // Div to show MediaItem
        var itemDiv = dojo.create('div', null, mediaItemDiv);
        var table = dojo.create('table', null, itemDiv);
        var tbody = dojo.create('tbody', null, table);
        var tr = dojo.create('tr', null, tbody);
        var td = dojo.create('td', null, tr);
        dojo.create('img', {src: mediaItem.url}, td);
        if (mediaItem.description) {
            tr = dojo.create('tr', null, tbody);
            td = dojo.create('td', null, tr);
            td.appendChild(dojo.doc.createTextNode(mediaItem.description));
        }

        divManager.showMediaItem();
    }

    /*
     * Popup to edit album.
     */
    function editAlbumPopup(album) {
        console.log('editAlbumPopup: ' + JSON.stringify(album));

        var title = (album == null ? 'Create' : 'Edit') + ' Album';
        var dialog = new dijit.Dialog({id: 'editAlbumPopup', title: title, onCancel: destroyDialog});
        dojo.body().appendChild(dialog.domNode);

        var formDiv = dojo.create('div', {id: 'editAlbumFormDiv'});
        var form = new dijit.form.Form({id: 'editAlbumForm'});
        formDiv.appendChild(form.domNode);
        var table = dojo.create('table', null, form.domNode);
        var tbody = dojo.create('tbody', null, table);
        var tr = dojo.create('tr', null, tbody);
        dojo.create('td', null, tr).appendChild(dojo.create('label', {'innerHTML': 'Title', 'for': 'title'}));
        dojo.create('td', null, tr).appendChild(
            new dijit.form.ValidationTextBox({
                name: 'title',
                value: album == null ? '' : album.title
            }).domNode
        );
        tr = dojo.create('tr', null, tbody);
        dojo.create('td', null, tr).appendChild(dojo.create('label', {'innerHTML': 'Thumnail URL', 'for': 'thumbnail'}));
        dojo.create('td', null, tr).appendChild(
            new dijit.form.ValidationTextBox({
                name: 'thumbnail',
                value: album == null ? '' : album.thumbnailUrl
            }).domNode
        );
        tr = dojo.create('tr', null, tbody);
        dojo.create('td', null, tr).appendChild(dojo.create('label', {'innerHTML': 'Description', 'for': 'description'}));
        dojo.create('td', null, tr).appendChild(
            new dijit.form.Textarea({
                name: 'description',
                value: album == null ? '' : album.description
            }).domNode
        );
        tr = dojo.create('tr', null, tbody);
        var buttonTd = dojo.create('td', {colspan: '2', align: 'center'}, tr);
        buttonTd.appendChild(new dijit.form.Button({
                label: 'Save',
                onClick: saveForm
            }).domNode
        );
        buttonTd.appendChild(new dijit.form.Button({
                label: 'Cancel',
                onClick: destroyDialog
            }).domNode
        );

        dialog.set('content', formDiv);
        dialog.show();

        function saveForm() {
            console.log('saveForm');
            var values = form.get('value');
            var newAlbum = {
                title: values.title,
                thumbnailUrl: values.thumbnail,
                description: values.description,
                ownerId: viewer.id  // TODO: bug? Albums service should set this
            };
            if (album == null) {
                social.createAlbum(viewer.id, newAlbum, function(response) {
                	publish('org.apache.shindig.album.created', newAlbum);
                    console.log('created album response: ' + JSON.stringify(response));
                    renderAlbumsByUser(viewer.id);
                });
            } else {
                social.updateAlbum(viewer.id, album.id, newAlbum, function(response) {
                	publish('org.apache.shindig.album.updated', newAlbum);
                    console.log('updated album response: ' + JSON.stringify(response));
                    renderAlbumsByUser(viewer.id);
                });
            }
            destroyDialog();
        }

        // Handles destroying the dialog popup
        function destroyDialog() {
            console.log('destroyDialog');
            dialog.hide();
            dialog.destroyRecursive(false);
            dialog.destroyRendering(false);
            dialog.destroy(false);
        }
    }

    /*
     * Popup to edit MediaItem.
     */
    function editMediaItemPopup(album, mediaItem) {
        console.log('editMediaItemPopup: ' + JSON.stringify(mediaItem));

        var albumId = mediaItem == null ? album.id : mediaItem.albumId;
        var title = (mediaItem == null ? 'Create' : 'Edit') + ' MediaItem';
        var dialog = new dijit.Dialog({id: 'editMediaItemPopup', title: title, onCancel: destroyDialog});
        dojo.body().appendChild(dialog.domNode);

        // Form div
        var formDiv = dojo.create('div', {id: 'editMediaItemFormDiv'});
        var form = new dijit.form.Form({id: 'editMediaItemForm'});
        formDiv.appendChild(form.domNode);
        var table = dojo.create('table', null, form.domNode);
        var tbody = dojo.create('tbody', null, table);
        var tr = dojo.create('tr', null, tbody);
        dojo.create('td', null, tr).appendChild(dojo.create('label', {'innerHTML': 'Title', 'for': 'title'}));
        dojo.create('td', null, tr).appendChild(
            new dijit.form.ValidationTextBox({
                name: 'title',
                value: mediaItem == null ? '' : mediaItem.title
            }).domNode
        );
        tr = dojo.create('tr', null, tbody);
        dojo.create('td', null, tr).appendChild(dojo.create('label', {'innerHTML': 'Description', 'for': 'description'}));
        dojo.create('td', null, tr).appendChild(
            new dijit.form.Textarea({
                name: 'description',
                value: mediaItem == null ? '' : mediaItem.description
            }).domNode
        );
        tr = dojo.create('tr', null, tbody);
        dojo.create('td', null, tr).appendChild(dojo.create('label', {'innerHTML': 'Type', 'for': 'type'}));
        dojo.create('td', null, tr).appendChild(
            new dijit.form.ValidationTextBox({
                name: 'type',
                value: mediaItem == null ? '' : mediaItem.type
            }).domNode
        );
        tr = dojo.create('tr', null, tbody);
        dojo.create('td', null, tr).appendChild(dojo.create('label', {'innerHTML': 'Thumnail URL', 'for': 'thumbnailUrl'}));
        dojo.create('td', null, tr).appendChild(
            new dijit.form.ValidationTextBox({
                name: 'thumbnailUrl',
                value: mediaItem == null ? '' : mediaItem.thumbnailUrl
            }).domNode
        );
        tr = dojo.create('tr', null, tbody);
        dojo.create('td', null, tr).appendChild(dojo.create('label', {'innerHTML': 'URL', 'for': 'url'}));
        dojo.create('td', null, tr).appendChild(
            new dijit.form.ValidationTextBox({
                name: 'url',
                value: mediaItem == null ? '' : mediaItem.url
            }).domNode
        );
        tr = dojo.create('tr', null, tbody);
        var buttonTd = dojo.create('td', {colspan: '2', align: 'center'}, tr);
        buttonTd.appendChild(new dijit.form.Button({
                label: 'Save',
                onClick: saveForm
            }).domNode
        );
        buttonTd.appendChild(new dijit.form.Button({
                label: 'Cancel',
                onClick: destroyDialog
            }).domNode
        );

        // Textarea div for JSON
        var textAreaDiv = dojo.create('div', {style: 'width:100%; height:100%;', id: 'textAreaDiv'});
        var textArea = new dijit.form.Textarea({value: JSON.stringify(mediaItem), rows: '20'});
        textAreaDiv.appendChild(textArea.domNode);

        // Put divs together
        var tabContainer = new dijit.layout.TabContainer({style: 'width:400px; height:300px;'});
        var formContentPane = new dijit.layout.ContentPane({title: 'Form', content: formDiv});
        tabContainer.addChild(formContentPane);
        var textAreaContentPane = new dijit.layout.ContentPane({title: 'JSON', content: textAreaDiv});
        tabContainer.addChild(textAreaContentPane);
        tabContainer.startup();
        var dialogDiv = dojo.create('div', null);
        dialogDiv.appendChild(tabContainer.domNode);

        dialog.set('content', dialogDiv);
        dialog.show();

        function saveForm() {
            console.log('saveForm mediaItem');
            var values = form.get('value');
            var newMediaItem = {
                title: values.title,
                description: values.description,
                type: values.type,
                thumbnailUrl: values.thumbnailUrl,
                url: values.url
            };
            if (newMediaItem.type == null || newMediaItem.type == '') newMediaItem.type = 'image';
            if (mediaItem == null) {
                social.createMediaItem(viewer.id, albumId, newMediaItem, function(response) {
                	publish('org.apache.shindig.mediaItem.created', newMediaItem);
                    console.log('created MediaItem response: ' + JSON.stringify(response));
                    social.getMediaItemsByAlbum(viewer.id, album.id, function(response) {
                        renderMediaItems(album, response.list);
                    });
                });
            } else {
                social.updateMediaItem(viewer.id, albumId, mediaItem.id, newMediaItem, function(response) {
                	publish('org.apache.shindig.mediaItem.updated', newMediaItem);
                    console.log('updated MediaItem response: ' + JSON.stringify(response));
                    social.getMediaItemsByAlbum(viewer.id, album.id, function(response) {
                        renderMediaItems(album, response.list);
                    });
                });
            }
            destroyDialog();
        }

        // Handles destroying the dialog popup
        function destroyDialog() {
            console.log('destroyDialog');
            dialog.hide();
            dialog.destroyRecursive(false);
            dialog.destroyRendering(false);
            dialog.destroy(false);
        }
    }

    /*
     * Popup to confirm that the user wants to delete album.
     */
    function deleteAlbumPopup(album) {
        console.log('deleteAlbumPopup');
        if (confirm("Delete '" + album.title + "'?")) {
            social.deleteAlbum(viewer.id, album.id, function(response) {
            	publish('org.apache.shindig.album.deleted', album);
                console.log('delete album response: ' + JSON.stringify(response));
                renderAlbumsByUser(viewer.id);
            });
        }
    }

    /*
     * Popup to confirm user wants to delete MediaItem.
     */
    function deleteMediaItemPopup(album, mediaItem) {
        console.log('deleteMediaItemPopup');
        var albumId = mediaItem.albumId;
        if (confirm("Delete '" + mediaItem.title + "'?")) {
            social.deleteMediaItem(viewer.id, albumId, mediaItem.id, function(response) {
            	publish('org.apache.shindig.mediaItem.deleted', mediaItem);
                console.log('delete mediaItem response: ' + JSON.stringify(response));
                social.getMediaItemsByAlbum(viewer.id, albumId, function(response) {
                    renderMediaItems(album, response.list);
                });
            });
        }
    }

    /*
     * Publishers.
     */
    function publish(topic, payload) {
    	gadgets.Hub.publish(topic, payload);
    }
}
