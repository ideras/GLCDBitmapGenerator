package glcd.GLCDImageLoader;

/**
 *
 * @author Ivan Deras
 */
public class Token
{

    private int kind;
    private String value;

    public Token(int kind, String value)
    {
        this.kind = kind;
        this.value = value;
    }

    public int getKind()
    {
        return kind;
    }

    public void setKind(int kind)
    {
        this.kind = kind;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "Token{" + "kind=" + kind + ", value=" + value + "}";
    }
    
    //Token IDs
    public static final int KEYWORD = 100;
    public static final int MODIFIER = 101;
    public static final int IDENTIFIER = 102;
    public static final int SYMBOL = 103;
    public static final int DEC_NUMBER = 104;
    public static final int HEX_NUMBER = 105;
    public static final int OPEN_BRACKET = 200;
    public static final int CLOSE_BRACKET = 201;
    public static final int OPEN_BRACE = 202;
    public static final int CLOSE_BRACE = 203;
    public static final int COMMA = 204;
    public static final int SEMICOLON = 205;
    public static final int EQUAL = 206;
    public static final int EOF = 999;
    
    //Symbol related token definitions
    public static Token OpenBracket = new Token(OPEN_BRACKET, "[");
    public static Token CloseBracket = new Token(CLOSE_BRACKET, "]");
    public static Token OpenBrace = new Token(OPEN_BRACE, "{");
    public static Token CloseBrace = new Token(CLOSE_BRACE, "}");
    public static Token Comma = new Token(COMMA, ",");
    public static Token Semicolon = new Token(SEMICOLON, ";");
    public static Token Equal = new Token(EQUAL, "=");
    public static Token Eof = new Token(EOF, "<EOF>");
}
