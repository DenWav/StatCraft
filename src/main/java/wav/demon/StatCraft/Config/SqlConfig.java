package wav.demon.StatCraft.Config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import wav.demon.StatCraft.Config.com.md_5.config.AnnotatedConfig;
import wav.demon.StatCraft.Config.com.md_5.config.ConfigComment;
import wav.demon.StatCraft.Config.com.md_5.config.NewLine;

@Data
@EqualsAndHashCode(callSuper = false)
public class SqlConfig extends AnnotatedConfig {

    public String hostname = "localhost";
    public String username = "statcraft";
    public String password = "";
    public String database = "statcraft";
    public String port = "3306";

    @NewLine
    @ConfigComment({"StatCraft will attempt to setup the database when it starts, but if there are already conflicting",
                    "tables that are improperly setup, it will only drop those tables if this settings is set to `true`.",
                    "StatCraft will not run unless all of the tables are setup correctly. It is advised to give StatCraft",
                    "its own database to work with, which is the simplest way to prevent conflicts."})
    public boolean forceSetup = false;
}
