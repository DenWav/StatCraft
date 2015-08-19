package wav.demon.StatCraft.Commands;

public class CommandAlreadyDefinedException extends RuntimeException {
    public CommandAlreadyDefinedException(String s) {
        super(s + " has already been defined.");
    }
}
