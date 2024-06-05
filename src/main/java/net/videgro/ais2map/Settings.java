package net.videgro.ais2map;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.videgro.ais2map.domain.IpHost;

@SuppressWarnings("deprecation")
public class Settings {
	private static final Logger LOGGER = LogManager.getRootLogger();
	
	public static final String ARGS_SPEECH_SYNTHESIZER_TEXT="<TEXT>";
	public static final String ARGS_SPEECH_SYNTHESIZER_OUTFILE="<OUTFILE>";
	
	private static final String LOCALHOST="127.0.0.1";
	
	private static final String OPTION_DEMO="demo";
	private static final String OPTION_DIR_RESOURCES="resources-dir";
	private static final String OPTION_DIR_LOGGING="logging-dir";
	private static final String OPTION_PORT_AIS="ais-port";
	private static final String OPTION_PORT_JSON="json-port";
	private static final String OPTION_ADDRESS_AIS="ais-address";
	private static final String OPTION_ADDRESS_JSON="json-address";
	private static final String OPTION_AIS_REPEATERS="ais-repeaters";
	private static final String OPTION_BIN_SPEECH_SYNTHESIZER="speech-synthesizer-bin";
	private static final String OPTION_ARGS_SPEECH_SYNTHESIZER="speech-synthesizer-args";
		
	private static final int DEFAULT_PORT_AIS=10110;
	private static final int DEFAULT_PORT_JSON=10111;
	private static final String DEFAULT_IP_ADDRESS_AIS=LOCALHOST;
	private static final String DEFAULT_IP_ADDRESS_JSON=LOCALHOST;
	private static final String DEFAULT_DIR_RESOURCES="src/main/resources/server/";
	private static final String DEFAULT_DIR_LOGS=DEFAULT_DIR_RESOURCES+"logs/";
	private static final String DEFAULT_BIN_SPEECH_SYNTHESIZER="/usr/bin/espeak";
	private static final String DEFAULT_ARGS_SPEECH_SYNTHESIZER="-vnl+f1 -w "+ARGS_SPEECH_SYNTHESIZER_OUTFILE+" '"+ARGS_SPEECH_SYNTHESIZER_TEXT+"' ";
			
	private boolean demoMode;
	private String dirLogging=DEFAULT_DIR_LOGS;
	private int portAis=DEFAULT_PORT_AIS;
	private int portJson=DEFAULT_PORT_JSON;
	private String dirResources=DEFAULT_DIR_RESOURCES;;
	private String binSpeechSynthesizer=DEFAULT_BIN_SPEECH_SYNTHESIZER;
	private String argsSpeechSynthesizer=DEFAULT_ARGS_SPEECH_SYNTHESIZER;
	private String ipAddressAisStr=DEFAULT_IP_ADDRESS_AIS; 
	private String ipAddressJsonStr=DEFAULT_IP_ADDRESS_JSON;

	private InetAddress ipAddressJson;
	private final List<IpHost> aisRepeaters=new ArrayList<>();
	
	@SuppressWarnings({"static-access"})
	private Options createCliOptions(){
		Options result = new Options();
		result.addOption(OptionBuilder.withLongOpt(OPTION_DEMO).withDescription("execute in demo mode").create());
		result.addOption(OptionBuilder.withLongOpt(OPTION_DIR_LOGGING).withArgName(OPTION_DIR_LOGGING).hasArg().withDescription("use given directory as logging location (default: "+DEFAULT_DIR_LOGS+"). Empty string to disable logging.").create());
		result.addOption(OptionBuilder.withLongOpt(OPTION_DIR_RESOURCES).withArgName(OPTION_DIR_RESOURCES).hasArg().withDescription("use given directory as resources location (default: "+DEFAULT_DIR_RESOURCES+").").create());
		result.addOption(OptionBuilder.withLongOpt(OPTION_BIN_SPEECH_SYNTHESIZER).withArgName(OPTION_BIN_SPEECH_SYNTHESIZER).hasArg().withDescription("use given executable as speech synthesizer (default: "+DEFAULT_BIN_SPEECH_SYNTHESIZER+"). Empty string to disable speech synthesizer.").create());
		result.addOption(OptionBuilder.withLongOpt(OPTION_ARGS_SPEECH_SYNTHESIZER).withArgName(OPTION_ARGS_SPEECH_SYNTHESIZER).hasArg().withDescription("use given arguments as arguments for speech synthesizer (default: "+DEFAULT_ARGS_SPEECH_SYNTHESIZER+").").create());
		result.addOption(OptionBuilder.withLongOpt(OPTION_ADDRESS_AIS).withArgName(OPTION_ADDRESS_AIS).hasArg().withDescription("use given IP address to bind AIS listener (default: "+DEFAULT_IP_ADDRESS_AIS+"). Empty string to use defaults.").create());
		result.addOption(OptionBuilder.withLongOpt(OPTION_ADDRESS_JSON).withArgName(OPTION_ADDRESS_JSON).hasArg().withDescription("use given IP address to send JSON messages (default: "+DEFAULT_IP_ADDRESS_JSON+"). Empty string to use defaults.").create());
		result.addOption(OptionBuilder.withLongOpt(OPTION_AIS_REPEATERS).withArgName(OPTION_AIS_REPEATERS).hasArg().withDescription("use given comma seperated IP addresses:ports to repeat AIS messages to. Empty string to disable repeater.").create());
		result.addOption(OptionBuilder.withLongOpt(OPTION_PORT_AIS).withArgName(OPTION_PORT_AIS).hasArg().withDescription("use given number as UDP port to listen for AIS messages ( = input / default: "+DEFAULT_PORT_AIS+").").withType(Number.class).create());
		result.addOption(OptionBuilder.withLongOpt(OPTION_PORT_JSON).withArgName(OPTION_PORT_JSON).hasArg().withDescription("use given number as UDP port to send JSON messages ( = ouutput / default: "+DEFAULT_PORT_JSON+").").withType(Number.class).create());
		return result;
	}
	
	public boolean parseCommandLine(final String[] args) {
		final String tag="parseCommandLine - ";

		boolean result=false;
		
		final CommandLine commandLine=parseArguments(args);		
		
		if (commandLine!=null){
			try{
				demoMode=commandLine.hasOption(OPTION_DEMO);
				
				if (commandLine.hasOption(OPTION_DIR_LOGGING)){
					dirLogging=commandLine.getOptionValue(OPTION_DIR_LOGGING);					
				}		
		
				if (commandLine.hasOption(OPTION_DIR_RESOURCES)){
					dirResources=commandLine.getOptionValue(OPTION_DIR_RESOURCES);					
				}
				
				if (commandLine.hasOption(OPTION_BIN_SPEECH_SYNTHESIZER)){
					binSpeechSynthesizer=commandLine.getOptionValue(OPTION_BIN_SPEECH_SYNTHESIZER);					
				}
				
				if (commandLine.hasOption(OPTION_ARGS_SPEECH_SYNTHESIZER)){
					argsSpeechSynthesizer=commandLine.getOptionValue(OPTION_ARGS_SPEECH_SYNTHESIZER);					
				}
		
				if (commandLine.hasOption(OPTION_PORT_AIS)){
					portAis=((Number)commandLine.getParsedOptionValue(OPTION_PORT_AIS)).intValue();					
				}
				
				if (commandLine.hasOption(OPTION_PORT_JSON)){
					portJson=((Number)commandLine.getParsedOptionValue(OPTION_PORT_JSON)).intValue();					
				}
				
				if (commandLine.hasOption(OPTION_AIS_REPEATERS)){
					parseAisRepeaters(commandLine.getOptionValue(OPTION_AIS_REPEATERS));					
				}

				if (commandLine.hasOption(OPTION_ADDRESS_AIS)){
					ipAddressAisStr=commandLine.getOptionValue(OPTION_ADDRESS_AIS);					
				}
				
				if (commandLine.hasOption(OPTION_ADDRESS_JSON)){
					ipAddressJsonStr=commandLine.getOptionValue(OPTION_ADDRESS_JSON);					
				}
				
				ipAddressJson = InetAddress.getByName(ipAddressJsonStr); 
				
				printSettings();
				
				result=true;
			} catch (ParseException e){
				LOGGER.error(tag,e);
			} catch (UnknownHostException e){
				LOGGER.error(tag,e); 
			}
		}
		return result;
	}
	
	private void parseAisRepeaters(final String optionAisRepeaters) {
		if (optionAisRepeaters!=null && !optionAisRepeaters.isEmpty()) {
			final String[] repeaters = optionAisRepeaters.trim().split(",");
			for (int i=0;i<repeaters.length;i++) {
				final String repeater=repeaters[i].trim();
				final String[] addressPort = repeater.split(":");
				try {
					final int port=Integer.valueOf(addressPort[1].trim());
					final InetAddress address = InetAddress.getByName(addressPort[0].trim());
					aisRepeaters.add(new IpHost(address,port));
				} catch (NumberFormatException | UnknownHostException e) {
					LOGGER.error("parseAisRepeaters: "+e.getMessage());
				}
			}
		}
	}

	private void printSettings(){
		final String prefix="Setting ";
		LOGGER.info(prefix+"AIS (input) to: "+ipAddressAisStr+":"+portAis);
		LOGGER.info(prefix+"JSON (output) to: "+ipAddressJsonStr+":"+portJson);
		LOGGER.info(prefix+"resources directory to: "+dirResources);
		
		if (!dirLogging.isEmpty()){
			LOGGER.info(prefix+"logging directory to: "+dirLogging);
		} else {
			LOGGER.warn("DISABLED AIS logging.");
		}
		
		if (!binSpeechSynthesizer.isEmpty()){
			LOGGER.info(prefix+"speech synthesizer to: "+binSpeechSynthesizer+" ("+argsSpeechSynthesizer+")");
		} else {
			LOGGER.warn("DISABLED speech synthesizer.");
		}

		if (!aisRepeaters.isEmpty()) {
			final StringBuilder builder=new StringBuilder();
			aisRepeaters.forEach(r -> builder.append(" ").append(r.toString()));
			LOGGER.info(prefix+"AIS repeaters:"+builder.toString());
		} else {
			LOGGER.warn("DISABLED AIS repeaters.");
		}
	}
	
	private CommandLine parseArguments(final String[] args){
		CommandLine result=null;
		CommandLineParser parser = new DefaultParser();
		try {
			result = parser.parse(createCliOptions(), args);
		} catch (ParseException e) {
			LOGGER.error("parseOptions: "+e.getMessage());
			//LOGGER.trace("parseOptions",e);
		} 
		
		return result;
	}

	public boolean isDemoMode() {
		return demoMode;
	}

	public String getDirLogging() {
		return dirLogging;
	}

	public int getPortAis() {
		return portAis;
	}

	public int getPortJson() {
		return portJson;
	}

	public String getDirResources() {
		return dirResources;
	}

	public String getBinSpeechSynthesizer() {
		return binSpeechSynthesizer;
	}

	public String getArgsSpeechSynthesizer() {
		return argsSpeechSynthesizer;
	}

	public String getIpAddressAisStr() {
		return ipAddressAisStr;
	}

	public String getIpAddressJsonStr() {
		return ipAddressJsonStr;
	}

	public InetAddress getIpAddressJson() {
		return ipAddressJson;
	}

	public List<IpHost> getAisRepeaters() {
		return aisRepeaters;
	}	
}
