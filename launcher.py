import subprocess

USER = "sxs210570"


REMOTE_COMMANDS = ";".join([
    "cd distributed-systems-project-1",
    "javac HelloWorld.java",
    "java HelloWorld",
    "exit"
])

if __name__ == "__main__":

    for node_id in range(1, 46):

        HOSTNAME = "dc{:02d}.utdallas.edu".format(node_id)
        SSH_COMMAND = f"ssh {USER}@{HOSTNAME}"
        FINAL_COMMAND = f"{SSH_COMMAND} {REMOTE_COMMANDS}"
        process_object = subprocess.run(FINAL_COMMAND.split())
