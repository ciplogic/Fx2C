/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infrastructure;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ciprian
 */
public class Utf8String {

    private final static Logger logger;
    public static Utf8String Empty = new Utf8String("");

    static {
        logger = Logger.getLogger(Utf8String.class.getName());
    }

    final int _hash;
    byte[] _textData;

    public Utf8String(String text) {
        byte[] textData;
        try {
            textData = text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            textData = null;
            logger.log(Level.SEVERE, null, ex);
        }
        _textData = textData;
        _hash = text.hashCode();
    }

    @Override
    public String toString() {
        try {
            return new String(_textData, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Utf8String.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!(other instanceof Utf8String)) {
            return false;
        }
        Utf8String otherText = (Utf8String) other;
        if (_textData == otherText._textData) {
            return true;
        }
        if (_hash != otherText._hash) {
            return false;
        }
        boolean result = Arrays.equals(_textData, otherText._textData);
        if (result) {
            otherText._textData = _textData;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return _hash;
    }

}
