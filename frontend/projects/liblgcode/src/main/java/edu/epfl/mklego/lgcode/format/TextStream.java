package edu.epfl.mklego.lgcode.format;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

public class TextStream {
    private final List<String> lines;
    
    public TextStream (InputStream stream) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);

        byte[] allBytes = bufferedInputStream.readAllBytes();
        String content  = new String(allBytes);

        Stream<String> lines = List.of(content.split("\n"))
                .stream()
                .map(line -> line.strip())
                .filter(line -> !line.equals(""));

        this.lines = lines.toList();
    }

    private int lineId = 0;
    private int offset = 0;

    private String command = null;
    public String getCommand () {
        return command;
    }

    private String getLine () {
        return lines.get(lineId);
    }

    public String readWord () throws ParseException {
        String line = getLine();
        while (offset < line.length() && Character.isWhitespace(line.charAt(offset)))
            offset ++;
        if (offset == line.length())
            throw new ParseException("No more words on line: " + line);
    
        int start = offset;
        while (offset < line.length() && !Character.isWhitespace(line.charAt(offset)))
            offset ++;
        
        return line.substring(start, offset);
    }

    public boolean beginLine () throws ParseException {
        command = null;
        if (lineId >= lines.size()) return false;
    
        command = readWord();
        return true;
    }
    public void endLine () {
        lineId ++;
        offset = 0;
    }

    public int readInt () throws ParseException {
        String word = readWord();

        try {
            return Integer.parseInt(word);
        } catch (NumberFormatException exception) {
            throw new ParseException("Invalid integer format (" + word + ") on line: " + getLine());
        }
    }
    public String readEscapedString () throws ParseException {
        String line = getLine();
        while (offset < line.length() && Character.isWhitespace(line.charAt(offset)))
            offset ++;
            
        if (offset == line.length())
            throw new ParseException("No more strings on line: " + line);
    
        if (line.charAt(offset) != '"')
            throw new ParseException("Could not read a string on line, missing start '\"': " + line);
        
        offset ++;

        StringBuilder builder = new StringBuilder();
        while (offset < line.length()) {
            char chr = line.charAt(offset);
            offset ++;

            if (chr == '"') return builder.toString();

            if (chr == '\\') {
                if (offset == line.length()) break ;
                
                char nchr = line.charAt(offset);
                offset ++;

                System.out.println("ADD " + chr + " " + nchr);
                if (nchr == 't') builder.append('\t');
                else if (nchr == 'n') builder.append('\n');
                else builder.append(nchr);

                continue ;
            }

            System.out.println("ADD " + chr);
            builder.append(chr);
        }

        throw new ParseException("Could not read a string on line, unclosed of string: " + line);
    }

}
