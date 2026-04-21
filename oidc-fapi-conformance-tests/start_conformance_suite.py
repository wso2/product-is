import subprocess
import os
import sys


def start(file):
    try:
        os.chmod(file, 0o777)
        process = subprocess.Popen("sudo -E docker-compose -f " + file + " up", shell=True, stdout=subprocess.PIPE)
        while True:
            output = process.stdout.readline()
            if not output and process.poll() is not None:
                break
            if b'conformance-suite_server_1 exited with code 1' in output:
                raise Exception("Conformance suite failed to start")
            elif b'Starting application' in output:
                print("Server Started")
                break
            elif output:
                print(output.strip())
        rc = process.poll()
        return rc
    except FileNotFoundError:
        print(file + " not found")
        raise

# takes OIDC conformance suite url as an argument
start(sys.argv[1])
