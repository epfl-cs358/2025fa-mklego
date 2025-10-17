package edu.epfl.mklego.lgcode.config;

import org.junit.jupiter.api.Test;

import edu.epfl.mklego.lgcode.ExceptionGroup;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PlateSizeTests {
    @Test
    void testCreatePlateSize () {
        PlateSize plate = new PlateSize(16, 20);
        assertEquals( plate.width (), 16 );
        assertEquals( plate.height(), 20 );
    }

    @Test
    void testWriteTextPlateSize () throws IOException, ExceptionGroup {
        PlateSize plate = new PlateSize(16, 20);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        plate.writeText(stream);
        plate.verify();

        String result = new String( stream.toByteArray() );
        assertEquals(result, "\tplatesize 16 20\n");
    }
    @Test
    void testWriteBinaryPlateSize () throws IOException, ExceptionGroup {
        PlateSize plate = new PlateSize(16, 20);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        plate.writeBinary(stream);
        plate.verify();

        assertArrayEquals(stream.toByteArray(), new byte[] { 3, 16, 20 });
    }
    @Test
    void testWritePlateSize () throws IOException, ExceptionGroup {
        PlateSize plate = new PlateSize(16, 20);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        plate.write(stream, false);
        plate.verify();

        assertArrayEquals(stream.toByteArray(), new byte[] { 3, 16, 20 });
        plate = new PlateSize(16, 20);
        
        stream = new ByteArrayOutputStream();
        plate.write(stream, true);
        plate.verify();

        String result = new String( stream.toByteArray() );
        assertEquals(result, "\tplatesize 16 20\n");
    }
    @Test
    void testVerifyPlateSize () {
        PlateSize plate = new PlateSize(0, 1);
        try {
            plate.verify();
        } catch (ExceptionGroup e) {
            assertEquals(e.exceptions().size(), 1);
            assertEquals(
                e.exceptions().get(0).getMessage(), 
                "Invalid plate width 0, it should have values between 1 and 255");
        }
        plate = new PlateSize(256, 0);
        try {
            plate.verify();
        } catch (ExceptionGroup e) {
            assertEquals(e.exceptions().size(), 2);
            assertEquals(
                e.exceptions().get(0).getMessage(), 
                "Invalid plate width 256, it should have values between 1 and 255");
            assertEquals(
                e.exceptions().get(1).getMessage(), 
                "Invalid plate height 0, it should have values between 1 and 255");
        }
        plate = new PlateSize(1, 256);
        try {
            plate.verify();
        } catch (ExceptionGroup e) {
            assertEquals(e.exceptions().size(), 1);
            assertEquals(
                e.exceptions().get(0).getMessage(), 
                "Invalid plate height 256, it should have values between 1 and 255");
        }
    }
}
