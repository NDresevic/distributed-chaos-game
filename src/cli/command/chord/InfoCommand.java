package cli.command.chord;

import app.AppConfig;
import cli.command.CLICommand;

public class InfoCommand implements CLICommand {

	@Override
	public String commandName() {
		return "info";
	}

	@Override
	public void execute(String args) {
		AppConfig.timestampedStandardPrint("My info: " + AppConfig.myServentInfo);
	}

}
