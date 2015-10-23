package com.bitmovin.bitcodin.examples;

import com.bitmovin.bitcodin.api.BitcodinApi;
import com.bitmovin.bitcodin.api.exception.BitcodinApiException;
import com.bitmovin.bitcodin.api.input.HTTPInputConfig;
import com.bitmovin.bitcodin.api.input.Input;
import com.bitmovin.bitcodin.api.job.*;
import com.bitmovin.bitcodin.api.media.*;

public class CreateCroppedVideoJob {

    public static void main(String[] args) throws InterruptedException {
        
        /* Create BitcodinApi */
        String apiKey = "YOUR_API_KEY";
        BitcodinApi bitApi = new BitcodinApi(apiKey);
        
        /* Create URL Input */
        HTTPInputConfig httpInputConfig = new HTTPInputConfig();
        httpInputConfig.url = "http://eu-storage.bitcodin.com/inputs/Sintel.2010.720p.mkv";

        Input input;
        try {
            input = bitApi.createInput(httpInputConfig);
        } catch (BitcodinApiException e) {
            System.out.println("Could not create input: " + e.getMessage());
            return;
        }
        
        System.out.println("Created Input: " + input.filename);
        
        /* Create EncodingProfile */
        VideoStreamConfig videoConfig = new VideoStreamConfig();
        videoConfig.bitrate = 1 * 1024 * 1024;
        videoConfig.width = 640;
        videoConfig.height = 480;
        videoConfig.profile = Profile.MAIN;
        videoConfig.preset = Preset.STANDARD;

        CroppingConfig croppingConfig = new CroppingConfig();
        croppingConfig.bottom = 100;  // 100 pixel of the bottom from the input video will be cropped, before encoding starts.
        croppingConfig.top    = 100;  // 100 pixel of the top from the input video will be cropped, before encoding starts.
        croppingConfig.right  = 5;    // 5 pixel of the right side from the input video will be cropped, before encoding starts.
        croppingConfig.left   = 5;    // 5 pixel of the left side from the input video will be cropped, before encoding starts.

        EncodingProfileConfig encodingProfileConfig = new EncodingProfileConfig();
        encodingProfileConfig.name = "JUnitTestProfile";
        encodingProfileConfig.videoStreamConfigs.add(videoConfig);
        encodingProfileConfig.croppingConfig = croppingConfig;
        
        EncodingProfile encodingProfile;
        try {
            encodingProfile = bitApi.createEncodingProfile(encodingProfileConfig);
        } catch (BitcodinApiException e) {
            System.out.println("Could not create encoding profile: " + e.getMessage());
            return;
        }
        
        /* Create Job */
        JobConfig jobConfig = new JobConfig();
        jobConfig.encodingProfileId = encodingProfile.encodingProfileId;
        jobConfig.inputId = input.inputId;
        jobConfig.manifestTypes.addElement(ManifestType.MPEG_DASH_MPD);
        jobConfig.manifestTypes.addElement(ManifestType.HLS_M3U8);
        jobConfig.speed = Speed.STANDARD;

        Job job;
        try {
            job = bitApi.createJob(jobConfig);
        } catch (BitcodinApiException e) {
            System.out.println("Could not create job: " + e.getMessage());
            return;
        }
        
        JobDetails jobDetails;
        
        do {
            try {
                jobDetails = bitApi.getJobDetails(job.jobId);
                System.out.println("Status: " + jobDetails.status.toString() +
                                   " - Enqueued Duration: " + jobDetails.enqueueDuration + "s" +
                                   " - Realtime Factor: " + jobDetails.realtimeFactor +
                                   " - Encoded Duration: " + jobDetails.encodedDuration + "s" +
                                   " - Output: " + jobDetails.bytesWritten/1024/1024 + "MB");
            } catch (BitcodinApiException e) {
                System.out.println("Could not get any job details");
                return;
            }
            
            if (jobDetails.status == JobStatus.ERROR) {
                System.out.println("Error during transcoding");
                return;
            }
            
            Thread.sleep(2000);
            
        } while (jobDetails.status != JobStatus.FINISHED);
        
        System.out.println("Job with ID " + job.jobId + " finished successfully!");
    }
}
