/**
 * Copyright 2017 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.ibm.watson.developer_cloud.natural_language_classifier.v1;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.google.gson.JsonObject;

import com.ibm.watson.developer_cloud.WatsonServiceUnitTest;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classification;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classifier;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.ClassifierList;

import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.ClassifyOptions;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.CreateClassifierOptions;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.DeleteClassifierOptions;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.GetClassifierOptions;
import org.junit.Before;
import org.junit.Test;

import okhttp3.mockwebserver.RecordedRequest;

/**
 * The Class NaturalLanguageClassifierTest.
 */
public class NaturalLanguageClassifierTest extends WatsonServiceUnitTest {
  private static final String TEXT = "text";
  private static final String CLASSIFIERS_PATH = "/v1/classifiers";
  private static final String CLASSIFY_PATH = "/v1/classifiers/%s/classify";
  private static final String RESOURCE = "src/test/resources/natural_language_classifier/";

  private ClassifierList classifiers;
  private Classifier classifier;
  private Classification classification;

  private String classifierId;
  private NaturalLanguageClassifier service;

  /*
   * (non-Javadoc)
   *
   * @see com.ibm.watson.developer_cloud.WatsonServiceTest#setUp()
   */
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    service = new NaturalLanguageClassifier();
    service.setApiKey("");
    service.setEndPoint(getMockWebServerUrl());

    classifierId = "foo";
    classifiers = loadFixture(RESOURCE + "classifiers.json", ClassifierList.class);
    classifier = loadFixture(RESOURCE + "classifier.json", Classifier.class);
    classification = loadFixture(RESOURCE + "classification.json", Classification.class);
  }

  /**
   * Test classify.
   *
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testClassify() throws InterruptedException {
    final JsonObject contentJson = new JsonObject();
    contentJson.addProperty(TEXT, classification.getText());

    final String path = String.format(CLASSIFY_PATH, classifierId);

    server.enqueue(jsonResponse(classification));
    ClassifyOptions classifyOptions = new ClassifyOptions.Builder(classifierId,classification.getText()).build();
    final Classification result = service.classify(classifyOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals(path, request.getPath());
    assertEquals("POST", request.getMethod());
    assertEquals(contentJson.toString(), request.getBody().readUtf8());
    assertEquals(classification, result);
  }

  /**
   * Test get classifier.
   *
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testGetClassifier() throws InterruptedException {
    server.enqueue(jsonResponse(classifier));
    GetClassifierOptions getOptions = new GetClassifierOptions.Builder(classifierId).build();
    final Classifier response = service.getClassifier(getOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals(CLASSIFIERS_PATH + "/" + classifierId, request.getPath());
    assertEquals(classifier, response);
  }

  /**
   * Test list classifiers.
   *
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testListClassifiers() throws InterruptedException {
    server.enqueue(jsonResponse(classifiers));
    final ClassifierList response = service.listClassifiers().execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals(CLASSIFIERS_PATH, request.getPath());
    assertEquals(classifiers, response);
  }

  /**
   * Test create classifier.
   *
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testCreateClassifier() throws InterruptedException,FileNotFoundException {
    server.enqueue(jsonResponse(classifier));
    InputStream trainingData = new FileInputStream(new File(RESOURCE + "weather_data_train.csv"));
    InputStream metadata = new ByteArrayInputStream("{\"language\":\"en\",\"name\":\"My Classifier\"}".getBytes());
    CreateClassifierOptions createOptions = new CreateClassifierOptions.Builder()
        .trainingData(trainingData)
        .metadata(metadata)
        .build();
    final Classifier response = service.createClassifier(createOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals(CLASSIFIERS_PATH, request.getPath());
    assertEquals(classifier, response);
  }

  /**
   * Test delete classifier.
   *
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testDeleteClassifier() throws InterruptedException {
    DeleteClassifierOptions options = new DeleteClassifierOptions.Builder(classifierId).build();
    service.deleteClassifier(options);
  }

  // START NEGATIVE TESTS
  /**
   * Test null classifier.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNullClassifier() {
    ClassifyOptions options = new ClassifyOptions.Builder().text("test").build();
    service.classify(options).execute();
  }

  /**
   * Test null text.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNullText() {
    ClassifyOptions options = new ClassifyOptions.Builder().classifierId(classifierId).build();
    service.classify(options).execute();
  }

  /**
   * Test null delete classifier.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNullDeleteClassifier() {
    DeleteClassifierOptions options = new DeleteClassifierOptions.Builder().build();
    service.deleteClassifier(options).execute();
  }

  /**
   * Test null training data file.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNullTrainingDataFile() throws FileNotFoundException {
    CreateClassifierOptions options = new CreateClassifierOptions.Builder().build();
    service.createClassifier(options).execute();
  }

}
