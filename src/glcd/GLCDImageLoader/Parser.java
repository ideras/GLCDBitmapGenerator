package glcd.GLCDImageLoader;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Ivan Deras
 */
public class Parser
{
    private final Lexer lexer;
    private Token token;

    public Parser(Lexer lexer) throws GLCDImageLoaderException, IOException
    {
        this.lexer = lexer;
        token = lexer.nextToken();
    }

    private void match(int tokenKind, String tokenName) throws GLCDImageLoaderException, IOException
    {
        if (token.getKind() != tokenKind)
            throw new GLCDImageLoaderException("Expected " + tokenName + ", found " + token.toString());

        token = lexer.nextToken();
    }

    public RawImageInfo getImageInfo() throws GLCDImageLoaderException, IOException
    {
        ArrayList<Integer> pixelArray = new ArrayList<Integer>();
        int arraySize = -1;
        String name = "";

        if (token.getKind() == Token.EOF)
            return null;
        
        match(Token.KEYWORD, "keyword");
        while (token.getKind() == Token.KEYWORD)
            token = lexer.nextToken();
        
        name = token.getValue();
        match(Token.IDENTIFIER, "identifier");
        match(Token.OPEN_BRACKET, "'['");

        switch (token.getKind()) {
            case Token.DEC_NUMBER:
                arraySize = Integer.parseInt(token.getValue(), 10);
                token = lexer.nextToken();
                match(Token.CLOSE_BRACKET, "']'");
                break;
            case Token.HEX_NUMBER:
                arraySize = Integer.parseInt(token.getValue(), 16);
                token = lexer.nextToken();
                match(Token.CLOSE_BRACKET, "']'");
                break;
            default:
                match(Token.CLOSE_BRACKET, "']'");
        }

        if (token.getKind() == Token.IDENTIFIER)
            match(Token.IDENTIFIER, "identifier");

        match(Token.EQUAL, "'='");
        match(Token.OPEN_BRACE, "'{'");

        while (true) {
            int value = 0;
            switch (token.getKind()) {
                case Token.DEC_NUMBER:
                    value = Integer.parseInt(token.getValue(), 10);
                    break;
                case Token.HEX_NUMBER: {
                    String hexString = token.getValue().substring(2);
                    value = Integer.parseInt(hexString, 16);
                    break;
                }
                case Token.CLOSE_BRACE:
                    break;
                default:
                    throw new GLCDImageLoaderException("Expected decimal or hexadecimal number, found " + token.toString());
            }

            if (token != Token.CloseBrace) {
                pixelArray.add(new Integer(value));
                token = lexer.nextToken();
                if (token == Token.Comma) {
                    token = lexer.nextToken();
                    continue;
                } else if (token == Token.CloseBrace)
                    break;
                else
                    throw new GLCDImageLoaderException("Expected ',' or '}' number, found " + token.toString());
            } else
                break;
        }

        match(Token.CLOSE_BRACE, "'}'");
        
        if (token.getKind() == Token.SEMICOLON)
            match(Token.SEMICOLON, "';'");
        

        return new RawImageInfo(pixelArray, arraySize, name);
    }
}
