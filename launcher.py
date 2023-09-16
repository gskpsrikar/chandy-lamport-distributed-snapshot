import subprocess

USER = "sxs210570"
HOSTNAME = "dc01.utdallas.edu"

SSH_COMMAND = f"ssh {USER}@{HOSTNAME}"
REMOTE_COMMANDS = ";".join([
    "cd distributed-systems-project-1",
    "javac HelloWorld.java",
    "java HelloWorld",
    "exit"
])

if __name__ == "__main__":
    FINAL_COMMAND = f"{SSH_COMMAND} {REMOTE_COMMANDS}"
    process_object = subprocess.run(FINAL_COMMAND.split())
