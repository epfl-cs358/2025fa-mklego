package edu.epfl.mklego.lgcode.format;

public enum CommandKinds {
    SET_SECTION(CommandKindIds.SET_SECTION_CMD_ID, "."),
    ADD_COLOR  (CommandKindIds.ADD_COLOR_CMD_ID, "addcolor"),
    ADD_BRICK  (CommandKindIds.ADD_BRICK_CMD_ID, "addbrick"),
    PLATE_SIZE (CommandKindIds.PLATE_SIZE_CMD_ID, "platesize"),
    GRAB_BRICK (CommandKindIds.GRAB_BRICK_CMD_ID, "grabbrick"),
    DROP_BRICK (CommandKindIds.DROP_BRICK_CMD_ID, "dropbrick"),
    ROTATE     (CommandKindIds.ROTATE_CMD_ID, "rotate"),
    MOVE       (CommandKindIds.MOVE_CMD_ID, "move");

    public final String commandPrefix;
    public final int    commandId;

    private CommandKinds (int commandId, String commandPrefix) {
        this.commandId     = commandId;
        this.commandPrefix = commandPrefix;
    }
}
