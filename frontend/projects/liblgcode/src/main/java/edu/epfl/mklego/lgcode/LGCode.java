package edu.epfl.mklego.lgcode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import edu.epfl.mklego.lgcode.config.AddBrick;
import edu.epfl.mklego.lgcode.config.AddColor;
import edu.epfl.mklego.lgcode.config.PlateSize;
import edu.epfl.mklego.lgcode.format.CommandKindIds;
import edu.epfl.mklego.lgcode.format.ParseException;
import edu.epfl.mklego.lgcode.format.Serializable;
import edu.epfl.mklego.lgcode.format.SetSection;
import edu.epfl.mklego.lgcode.format.TextStream;
import edu.epfl.mklego.lgcode.print.Drop;
import edu.epfl.mklego.lgcode.print.Grab;
import edu.epfl.mklego.lgcode.print.Move;
import edu.epfl.mklego.lgcode.print.Rotate;

public class LGCode implements Serializable {
    private final List<Serializable> commands;

    public LGCode (List<Serializable> commands) {
        this.commands = List.copyOf(commands);
    }

    @Override
    public void verify() throws ExceptionGroup {}

    @Override
    public void writeText(OutputStream stream) throws IOException {
        stream.write("T-LGCODE\n".getBytes());
        
        for (Serializable command : commands)
            command.writeText(stream);
    }

    @Override
    public void writeBinary(OutputStream stream) throws IOException {
        stream.write("B-LGCODE".getBytes());
        
        for (Serializable command : commands)
            command.writeBinary(stream);
    }

    @FunctionalInterface
    private static interface TextSerializableFactory {
        public Serializable generate (TextStream stream) throws IOException, ParseException;
    }

    private static final TextSerializableFactory[] textFactories = new TextSerializableFactory[] {
        (stream) -> SetSection.readText(stream),

        (stream) -> PlateSize.readText(stream),
        (stream) -> AddColor.readText(stream),
        (stream) -> AddBrick.readText(stream),

        (stream) -> Drop.readText(stream),
        (stream) -> Grab.readText(stream),
        (stream) -> Move.readText(stream),
        (stream) -> Rotate.readText(stream),
    };

    public static LGCode read (InputStream stream) throws IOException, ParseException {
        byte[] baseBytes = stream.readNBytes(8);
    
        if (new String(baseBytes).equals("T-LGCODE"))
            return readText(stream);
        if (new String(baseBytes).equals("B-LGCODE"))
            return readBinary(stream);
        
        throw new ParseException("Could not recognize magic bytes 'T-LGCODE' or 'B-LGCODE': '" + new String(baseBytes) + "'");
    }
    public static LGCode readText (InputStream stream) throws IOException, ParseException {
        List<Serializable> commands = new ArrayList<>();
        
        TextStream textStream = new TextStream(stream);

        while (textStream.beginLine()) {
            Serializable ser = null;
            
            for (TextSerializableFactory factory : textFactories) {
                Serializable res = factory.generate(textStream);
                if (res == null) continue ;

                ser = res;
                break ;
            }

            if (ser == null)
                throw new ParseException("Could not recognize command " + textStream.getCommand());
            
            textStream.endLine();
            commands.add(ser);
        }

        return new LGCode(commands);
    }
    public static LGCode readBinary (InputStream stream) throws IOException, ParseException {
        List<Serializable> commands = new ArrayList<>();
        
        int cmd = 0;
        while ((cmd = stream.read()) != -1) {
            Serializable ser = null;

            System.out.println("COMMAND ID " + cmd);
            
            switch (cmd) {
                case CommandKindIds.SET_SECTION_CMD_ID:
                    ser = SetSection.readBinary(stream, cmd);
                    break;

                case CommandKindIds.PLATE_SIZE_CMD_ID: 
                    ser = PlateSize.readBinary(stream, cmd);
                    break ;
                case CommandKindIds.ADD_COLOR_CMD_ID:  
                    ser = AddColor.readBinary(stream, cmd);
                    break ;
                case CommandKindIds.ADD_BRICK_CMD_ID:  
                    ser = AddBrick.readBinary(stream, cmd);
                    break ;

                case CommandKindIds.DROP_BRICK_CMD_ID: 
                    ser = Drop.readBinary(stream, cmd);
                    break ;
                case CommandKindIds.GRAB_BRICK_CMD_ID: 
                    ser = Grab.readBinary(stream, cmd);
                    break ;
                case CommandKindIds.MOVE_CMD_ID:       
                    ser = Move.readBinary(stream, cmd);
                    break ;
                case CommandKindIds.ROTATE_CMD_ID:     
                    ser = Rotate.readBinary(stream, cmd);
                    break ;
            };

            System.out.println(ser);

            if (ser == null)
                throw new ParseException("Could not recognize command " + cmd);
            
            commands.add(ser);
        }

        return new LGCode(commands);
    }
    public List<edu.epfl.mklego.lgcode.format.Serializable> getCommands() {
    return commands;
}

}
