package wav.demon.StatCraft.Config.com.md_5.config;

public class StandaloneComment {

    public String[] value;

    public StandaloneComment(String... lines) {
        value = lines;
    }

    public StandaloneComment(String line) {
        value = new String[] {line};
    }
}
