package org.kie.trusty.xai.explainer.local.saliency;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import io.swagger.client.api.LocalApi;
import org.kie.trusty.xai.explainer.utils.DataUtils;
import org.kie.trusty.xai.explainer.utils.LinearClassifier;
import org.kie.trusty.xai.handler.ApiClient;
import org.kie.trusty.xai.handler.ApiException;
import org.kie.trusty.xai.handler.Configuration;
import org.kie.trusty.xai.model.Feature;
import org.kie.trusty.xai.model.FeatureImportance;
import org.kie.trusty.xai.model.ModelInfo;
import org.kie.trusty.xai.model.Prediction;
import org.kie.trusty.xai.model.PredictionInput;
import org.kie.trusty.xai.model.PredictionOutput;
import org.kie.trusty.xai.model.Saliency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LIME-ish explanation implementation.
 * Perturbations to data inputs are performed by randomly dropping features (setting them to 0).
 */
public class LIMEishSaliencyExplanationProvider implements SaliencyLocalExplanationProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * no. of samples to be generated for the local linear classifier model training
     */
    private final int noOfSamples;
    private final LocalApi apiInstance;

    LIMEishSaliencyExplanationProvider(LocalApi apiInstance, int noOfSamples) {
        this.apiInstance = apiInstance;
        this.noOfSamples = noOfSamples;
    }

    public LIMEishSaliencyExplanationProvider() {
        this(100);
    }

    public LIMEishSaliencyExplanationProvider(int noOfSamples) {
        this(new LocalApi(Configuration.getDefaultApiClient()), noOfSamples);
    }

    @Override
    public Saliency explain(Prediction prediction) {
        long start = System.currentTimeMillis();
        Saliency saliency = new Saliency();
        ModelInfo info = prediction.getInfo();
        try {
            apiInstance.getApiClient().setBasePath(info.getEndpoint());
            Collection<Prediction> training = new LinkedList<>();
            List<PredictionInput> perturbedInputs = new LinkedList<>();
            for (int i = 0; i < noOfSamples; i++) {
                perturbedInputs.add(DataUtils.perturbDrop(prediction.getInput()));
            }
            List<PredictionOutput> predictionOutputs = apiInstance.predict(perturbedInputs);

            for (int i = 0; i < perturbedInputs.size(); i++) {
                Prediction perturbedDataPrediction = new Prediction();
                perturbedDataPrediction.setInfo(info);
                perturbedDataPrediction.setInput(perturbedInputs.get(i));
                perturbedDataPrediction.setOutput(predictionOutputs.get(i));
                training.add(perturbedDataPrediction);
            }

            List<Feature> features = prediction.getInput().getFeatures();
            LinearClassifier linearClassifier = new LinearClassifier(features.size());
            linearClassifier.fit(training);
            double[] weights = linearClassifier.getWeights();
            List<FeatureImportance> saliencyMap = new LinkedList<>();
            for (int i = 0; i < weights.length; i++) {
                FeatureImportance featureImportance = new FeatureImportance();
                featureImportance.setFeature(features.get(i));
                featureImportance.setScore(BigDecimal.valueOf(weights[i]));
                saliencyMap.add(featureImportance);
            }
            saliencyMap.sort((o1, o2) -> o2.getScore().compareTo(o1.getScore()));

            saliency.setFeatureImportances(saliencyMap);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        long end = System.currentTimeMillis();
        logger.info("explanation time: {}ms", (end - start));
        return saliency;
    }
}
