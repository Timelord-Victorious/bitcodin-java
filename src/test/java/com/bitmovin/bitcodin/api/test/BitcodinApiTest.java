package com.bitmovin.bitcodin.api.test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.bitmovin.bitcodin.api.BitcodinApi;
import com.bitmovin.bitcodin.api.exception.BitcodinApiException;
import com.bitmovin.bitcodin.api.input.HTTPInputConfig;
import com.bitmovin.bitcodin.api.input.Input;
import com.bitmovin.bitcodin.api.input.InputList;
import com.bitmovin.bitcodin.api.job.Job;
import com.bitmovin.bitcodin.api.job.JobConfig;
import com.bitmovin.bitcodin.api.job.JobList;
import com.bitmovin.bitcodin.api.media.EncodingProfile;
import com.bitmovin.bitcodin.api.media.EncodingProfileConfig;
import com.bitmovin.bitcodin.api.media.EncodingProfileList;
import com.bitmovin.bitcodin.api.media.VideoStreamConfig;
import com.bitmovin.bitcodin.api.output.Output;
import com.bitmovin.bitcodin.api.output.OutputList;
import com.bitmovin.bitcodin.api.statistics.Statistic;
import com.bitmovin.bitcodin.api.transfer.TransferConfig;

public class BitcodinApiTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    private Settings settings;

    public BitcodinApiTest() throws FileNotFoundException {
        this.settings = Settings.getInstance();
    }

    @Test
    public void testApiKeyGetter() {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        assertEquals(this.settings.apikey, bitApi.getKey());
    }

    public Input createSintelInput() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        HTTPInputConfig httpInputConfig = new HTTPInputConfig();
        httpInputConfig.url = "http://ftp.nluug.nl/pub/graphics/blender/demo/movies/Sintel.2010.720p.mkv";
        
        return bitApi.createInput(httpInputConfig);
    }
    @Test
    public void createInput() throws BitcodinApiException {
        Input input = this.createSintelInput();

        assertEquals(input.filename, "Sintel.2010.720p.mkv");
        assertEquals(input.mediaConfigurations.size(), 2);
        assertEquals(input.mediaConfigurations.get(0).width, 1280);
        assertEquals(input.mediaConfigurations.get(0).height, 544);
    }

    @Test
    public void listInputs() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        Input input = this.createSintelInput();
        InputList inputList = bitApi.listInputs(0);
        Input lastRecentInput = inputList.inputs.get(0);
        
        assertEquals(lastRecentInput.filename, input.filename);
        assertEquals(lastRecentInput.inputId, input.inputId);
    }

    @Test
    public void getInput() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        Input input = this.createSintelInput();
        Input sameInput = bitApi.getInput(input.inputId);

        assertEquals(input.filename, sameInput.filename);
        assertEquals(input.inputId, sameInput.inputId);
    }

    @Test
    public void deleteInput() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        Input input = this.createSintelInput();

        bitApi.deleteInput(input.inputId);
        /* TODO: fix API input delete is not working */
        //assertNull(bitApi.getInput(input.inputId));
    }

    @Test
    public void createS3Output() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        bitApi.createS3Output(this.settings.s3OutputEUWest);
    }

    @Test
    public void createGCSOutput() throws BitcodinApiException {
        /* TODO Create public GCS bucket*/
    }

    @Test
    public void createFTPOutput() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        bitApi.createFTPOutput(this.settings.ftpOutput);
    }

    @Test
    public void listOutputs() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        Output output = bitApi.createFTPOutput(this.settings.ftpOutput);
        OutputList outputList = bitApi.listOutputs(0);
        Output lastRecentOutput = outputList.outputs.get(0);
        
        assertEquals(lastRecentOutput.outputId, output.outputId);
    }

    @Test
    public void getOutput() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        Output output = bitApi.createFTPOutput(this.settings.ftpOutput);
        Output sameOutput = bitApi.getOutput(output.outputId);

        assertEquals(output.name, sameOutput.name);
        assertEquals(output.outputId, sameOutput.outputId);
    }

    @Test
    public void deleteOutput() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        Output output = bitApi.createFTPOutput(this.settings.ftpOutput);

        bitApi.deleteOutput(output.outputId);
        
        thrown.expect(BitcodinApiException.class);
        thrown.expectMessage("Resource not available");
        bitApi.getOutput(output.outputId);
    }
    
    public EncodingProfileConfig createEncodingProfileConfig() {
        VideoStreamConfig videoConfig = new VideoStreamConfig();
        videoConfig.bitrate = 1 * 1024 * 1024;
        videoConfig.width = 640;
        videoConfig.height = 480;
        videoConfig.profile = "Main";
        videoConfig.preset = "Standard";

        EncodingProfileConfig encodingProfileConfig = new EncodingProfileConfig();
        encodingProfileConfig.name = "JUnitTestProfile";
        encodingProfileConfig.videoStreamConfigs.add(videoConfig);
        
        return encodingProfileConfig;
    }

    @Test
    public void createEncodingProfile() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        EncodingProfileConfig config = this.createEncodingProfileConfig();
        EncodingProfile encodingProfile = bitApi.createEncodingProfile(config);

        assertEquals(encodingProfile.videoStreamConfigs.get(0).width, config.videoStreamConfigs.get(0).width);
        assertEquals(encodingProfile.videoStreamConfigs.get(0).height, config.videoStreamConfigs.get(0).height);
    }

    @Test
    public void listEncodingProfiles() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        EncodingProfileConfig config = this.createEncodingProfileConfig();
        EncodingProfile encodingProfile = bitApi.createEncodingProfile(config);
        EncodingProfileList encodingProfileList = bitApi.listEncodingProfiles(0);
        EncodingProfile lastRecentProfile = encodingProfileList.profiles.get(0);

        assertEquals(lastRecentProfile.encodingProfileId, encodingProfile.encodingProfileId);
    }

    @Test
    public void getEncodingProfile() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        EncodingProfileConfig config = this.createEncodingProfileConfig();
        EncodingProfile encodingProfile = bitApi.createEncodingProfile(config);
        EncodingProfile sameProfile = bitApi.getEncodingProfile(encodingProfile.encodingProfileId);

        assertEquals(sameProfile.name, encodingProfile.name);
        assertEquals(sameProfile.encodingProfileId, encodingProfile.encodingProfileId);
    }
    
    public JobConfig createJobConfig() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        JobConfig jobConfig = new JobConfig();
        EncodingProfileConfig config = this.createEncodingProfileConfig();
        EncodingProfile encodingProfile = bitApi.createEncodingProfile(config);
        Input input = this.createSintelInput();
        
        jobConfig.encodingProfileId = encodingProfile.encodingProfileId;
        jobConfig.inputId = input.inputId;
        jobConfig.manifestTypes.addElement("mpd");
        jobConfig.manifestTypes.addElement("m3u8");
        
        return jobConfig;
    }

    @Test
    public void createJob() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        JobConfig jobConfig = this.createJobConfig();
        Job job = bitApi.createJob(jobConfig);

        assertEquals(job.status, "Enqueued");
    }

    @Test
    public void listJobs() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        JobConfig jobConfig = this.createJobConfig();
        Job job = bitApi.createJob(jobConfig);
        JobList jobList = bitApi.listJobs(0);
        Job lastRecentJob = jobList.jobs.get(0);

        assertEquals(lastRecentJob.jobId, job.jobId);
    }

    @Test
    public void getJob() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        JobConfig jobConfig = this.createJobConfig();
        Job job = bitApi.createJob(jobConfig);
        Job sameJob = bitApi.getJob(job.jobId);

        assertEquals(sameJob.jobId, job.jobId);
    }

    public void transfer (Output output) throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        JobList jobList = bitApi.listJobs(0);
        
        Job finishedJob = null;
        for (Job job : jobList.jobs) {
            if (job.status.equals("Finished")) {
                finishedJob = job; 
                break;
            }
        }
        
        assertNotNull(finishedJob);
        
        TransferConfig transferConfig = new TransferConfig();
        
        transferConfig.jobId = finishedJob.jobId;
        transferConfig.outputId = output.outputId;
        
        bitApi.transfer(transferConfig);
    }
    
    @Test
    public void transferToS3() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        Output s3Output = bitApi.createS3Output(this.settings.s3OutputEUWest);
        
        this.transfer(s3Output);
    }
    
    @Test
    public void transferToFTP() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        Output ftpOutput = bitApi.createFTPOutput(this.settings.ftpOutput);
        
        this.transfer(ftpOutput);
    }

    @Test
    public void listTransfers() throws BitcodinApiException {
        /* TODO 
         * cannot effectively be implemented without API fix
         * so that transfer returns at least id */
    }

    @Test
    public void getStatistics() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        Statistic stats = bitApi.getStatistics();

        assertNotNull(stats);
        
        /* TODO 
         * Does this call return monthly? Values must be redesigned */
    }

    @Test
    public void getStatisticsFromTo() throws BitcodinApiException {
        BitcodinApi bitApi = new BitcodinApi(this.settings.apikey);
        Statistic stats = bitApi.getStatistics("2015-06-01", "2015-06-10");
        assertNotNull(stats);
        
        /* TODO 
         * Range is not working -> fix in API */
    }
}
