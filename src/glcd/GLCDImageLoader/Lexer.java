package glcd.GLCDImageLoader;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 *
 * @author Ivan Deras
 */
public class Lexer
{

    private final BufferedInputStream in;

    public Lexer(BufferedInputStream in)
    {
        this.in = in;
    }

    private boolean isHexDigit(int ch)
    {
        return Character.isDigit(ch) || (ch >= 'A' && ch <= 'F')
                || (ch >= 'a' && ch <= 'f');
    }

    private void skipUntilEOL() throws IOException
    {
        int ch = in.read();
        while ((ch != '\n') && (ch != -1)) {
            ch = in.read();
        }
    }

    public Token nextToken() throws GLCDImageLoaderException, IOException
    {
        while (true) {
            int ch = in.read();

            while ((ch == '\r') || (ch == '\n') || (ch == ' ') || (ch == '\t'))
                ch = in.read();

            switch (ch) {
                case -1:
                    return Token.Eof;
                case '[':
                    return Token.OpenBracket;
                case ']':
                    return Token.CloseBracket;
                case '{':
                    return Token.OpenBrace;
                case '}':
                    return Token.CloseBrace;
                case ',':
                    return Token.Comma;
                case ';':
                    return Token.Semicolon;
                case '=':
                    return Token.Equal;
                case '#': {
                    skipUntilEOL();
                    continue;
                }
                case '/': {
                    in.mark(10);
                    ch = in.read();
                    if (ch == '/') {
                        skipUntilEOL();
                        continue;
                    } else if (ch == '*') {
                        int ch1 = in.read(), ch2;
                        while (true) {
                            ch2 = in.read();
                            if ((ch1 == '*') && (ch2 == '/'))
                                break;
                            else if (ch2 == -1)
                                break;
                            ch1 = ch2;
                        }
                        continue;
                    } else
                        return new Token(Token.SYMBOL, "/");
                }
                default: {
                    StringBuilder sb = new StringBuilder();
                    if (Character.isDigit(ch)) {
                        sb.append((char) ch);
                        in.mark(10);
                        ch = in.read();
                        if (ch == 'x' || ch == 'X') {
                            sb.append((char) ch);

                            in.mark(10);
                            ch = in.read();
                            while (isHexDigit(ch)) {
                                sb.append((char) ch);
                                in.mark(10);
                                ch = in.read();
                            }
                            in.reset();

                            return new Token(Token.HEX_NUMBER, sb.toString());
                        } else if (Character.isDigit(ch)) {
                            while (Character.isDigit(ch)) {
                                sb.append((char) ch);
                                in.mark(10);
                                ch = in.read();
                            }
                            in.reset();
                        } else {
                            in.reset();
                        }

                        return new Token(Token.DEC_NUMBER, sb.toString());
                    } else if (Character.isLetter(ch)) {
                        while (Character.isLetterOrDigit(ch) || ch == '_') {
                            sb.append((char) ch);
                            in.mark(10);
                            ch = in.read();
                        }
                        in.reset();

                        String id = sb.toString();
                        if (id.equals("unsigned")
                                || id.equals("char")
                                || id.equals("uint8_t")
                                || id.equals("const")
                                || id.equals("static")) {
                            return new Token(Token.KEYWORD, id);
                        }
                        return new Token(Token.IDENTIFIER, id);
                    } else
                        throw new GLCDImageLoaderException("Character not recognized: " + (char) ch);
                }
            }
        }
    }
}
