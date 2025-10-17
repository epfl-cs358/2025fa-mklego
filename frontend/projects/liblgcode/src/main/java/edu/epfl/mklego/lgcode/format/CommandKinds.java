package edu.epfl.mklego.lgcode.format;

public enum CommandKinds {
    SET_SECTION(0, "."),
    ADD_COLOR  (1, "addcolor"),
    ADD_BRICK  (2, "addbrick"),
    PLATE_SIZE (3, "platesize"),
    GRAB_BRICK (4, "grabbrick"),
    DROP_BRICK (5, "dropbrick"),
    ROTATE     (6, "rotate"),
    MOVE       (7, "move");

    public final String commandPrefix;
    public final int    commandId;

    private CommandKinds (int commandId, String commandPrefix) {
        this.commandId     = commandId;
        this.commandPrefix = commandPrefix;
    }
}
