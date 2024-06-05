package net.videgro.ais2map;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.videgro.ais2map.domain.Ship;

public class AudioCreator {
	private static final Logger LOGGER = LogManager.getRootLogger();
	
	private final Settings settings;
	
	public AudioCreator(final Settings settings){
		this.settings=settings;
	}
	
	public void create(final Ship ship){
		final String tag="createAudio - ";
		LOGGER.trace(tag+" for ship: "+ship.getName());
		
		final String text=ship.getName();
		final String outFile=settings.getDirResources()+"cache/audio/"+ship.getMmsi()+".wav";
		
		// if not exists outfile
		if (!new File(outFile).exists()){
			final String arguments=" "+settings.getArgsSpeechSynthesizer().replace(Settings.ARGS_SPEECH_SYNTHESIZER_TEXT,text).replace(Settings.ARGS_SPEECH_SYNTHESIZER_OUTFILE,outFile);
			LOGGER.debug("Creating audio file - "+settings.getBinSpeechSynthesizer()+arguments);

			try {
				ProcessBuilder pb = new ProcessBuilder("sh", "-c",settings.getBinSpeechSynthesizer()+arguments);				
				Process process = pb.start();
				int errCode = process.waitFor();
				LOGGER.trace(tag+"executed, any errors? " + (errCode == 0 ? "No" : "Yes"));
				LOGGER.trace(tag+"output:\n" + processOutput(process.getInputStream()));  
				ship.setAudioAvailable(true);
			} catch (IOException e) {
				LOGGER.error(e);
			} catch (InterruptedException e){
				LOGGER.error(e);
			}
		} else {
			LOGGER.debug("Audio file ("+outFile+") exists already.");
			ship.setAudioAvailable(true);
		}
	}
	
	private String processOutput(final InputStream inputStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + System.getProperty("line.separator"));
			}
		} finally {
			br.close();
		}
		return sb.toString();
	}
}
