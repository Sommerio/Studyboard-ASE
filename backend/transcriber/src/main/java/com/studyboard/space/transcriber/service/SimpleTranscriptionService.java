package com.studyboard.space.transcriber.service;

import com.studyboard.model.Document;
import com.studyboard.space.transcriber.service.interafaces.FilePreprocessor;
import com.studyboard.space.transcriber.service.interafaces.SpeechRecognitionService;
import com.studyboard.space.transcriber.service.interafaces.TranscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * This class implements TranscriptionService.
 * It can be used to transcribe long audion and video files.
 *
 */
@Service
public class SimpleTranscriptionService implements TranscriptionService {
    private final Logger logger = LoggerFactory.getLogger(SimpleTranscriptionService.class);

    @Autowired
    private FilePreprocessor filePreprocessor;
    @Autowired
    private SpeechRecognitionService speechRecognitionService;

    /**
     * This method is used to transcribe given file and store information in the DB.
     * If the service could not transcribe the file empty transcription will be saved.
     * @param document document representation of the file to be transcribed
     */
    @Override
    public void transcribe(Document document) {
        try{
            String outputDirectory = filePreprocessor.cutIntoChunks(document.getFilePath());
            String transcription = speechRecognitionService.transcribeFilesInDirectory(outputDirectory);
            logger.debug(transcription);
            cleanup(outputDirectory);
            // TODO Allow IllegalFormatException to pass
        } catch (Exception e){
            logger.error("Could not transcribe file {}", document.getFilePath(), e);
        }
    }

    private void cleanup(String tempDirectory){
        File path = new File(tempDirectory);
        if (!path.isDirectory()){
            return;
        }
        for (File file: path.listFiles()){
            file.delete();
        }
        path.delete();
    }
}
