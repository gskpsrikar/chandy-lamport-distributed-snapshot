import subprocess

USER = "sxs210570"
HOSTNAME = "dc01.utdallas.edu"

SSH_COMMAND = f"ssh {USER}@{HOSTNAME}"
REMOTE_COMMANDS = ";".join([
    "cd distributed-systems-project-1",
    "ls",
    "javac HelloWorld.java",
    "java HelloWorld",
    "exit"
])

if __name__ == "__main__":
    process_object = subprocess.run(REMOTE_COMMANDS.split())
    