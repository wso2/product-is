import xml.etree.ElementTree as ET
import sys
from _collections import defaultdict


# Retrieve the version tag name.
def get_version_tag(tag):
    return tag[2:len(tag) - 1]


try:
    exit_code = "invalid"
    repo_dir = sys.argv[1]
    is_dir = sys.argv[2]

    # Read component's pom
    comp_pom = ET.parse(repo_dir + '/pom.xml')
    comp_root = comp_pom.getroot()

    # Read identity server pom
    is_pom = ET.parse(is_dir + '/pom.xml')
    is_root = is_pom.getroot()

    ns_map = {"m": "http://maven.apache.org/POM/4.0.0"}

    # Get component's groupId, artifactId and version.
    comp_group_id = comp_root.find("./m:groupId", ns_map)
    comp_artifact_id = comp_root.find("./m:artifactId", ns_map)
    comp_version = comp_root.find("./m:version", ns_map)

    # Get all dependencies in IS
    dependencies = is_root.findall(".//m:dependencies/m:dependency", ns_map)

    # Default dictionary to keep groupId and version tag mapping.
    dependency_dict = defaultdict(set)

    # Get groupId-version tag mapping.
    for dependency in dependencies:
        grp_id = dependency.find('m:groupId', ns_map).text
        version = dependency.find('m:version', ns_map).text
        dependency_dict[grp_id].add(version)

    # Set related version tag as the exit code.
    if len(dependency_dict[comp_group_id.text]) == 1:
        version_tag = get_version_tag(list(dependency_dict[comp_group_id.text])[0])
        exit_code = version_tag
finally:
    sys.exit(exit_code)
