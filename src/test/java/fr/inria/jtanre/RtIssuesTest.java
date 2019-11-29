
package fr.inria.jtanre;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.jtanre.rt.RtEngine;
import fr.inria.jtanre.rt.core.RuntimeInformation;
import fr.inria.jtanre.rt.core.TestAnalysisResult;
import fr.inria.jtanre.rt.core.TestIntermediateAnalysisResult;
import fr.inria.main.CommandSummary;
import fr.inria.main.evolution.AstorMain;
import spoon.reflect.declaration.CtClass;

/**
 * 
 * @author Matias Martinez
 *
 */
public class RtIssuesTest {

	@Test
	public void testRT1_NPE_conf_issue4() throws Exception {

		String projectName = "blueocean-plugin";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_RottenExpected_issue8_java_faker() throws Exception {

		String projectName = "java-faker";

		List<TestIntermediateAnalysisResult> result = runRTinProject(projectName);
		result = result.stream().filter(e -> e.isRotten() || e.isSmokeTest()).collect(Collectors.toList());
		assertEquals(1, result.size());
		assertTrue(result.get(0).isRotten());

	}

	@Test
	public void testRT1_RottenExpected_issue8_java_webdrivermanager() throws Exception {

		String projectName = "webdrivermanager";

		List<TestIntermediateAnalysisResult> result = runRTinProject(projectName);

		result = result.stream().filter(e -> e.isRotten() || e.isSmokeTest()).collect(Collectors.toList());
		assertEquals(1, result.size());
		assertTrue(result.get(0).isRotten());

	}

	@Test
	public void testRT1_RottenExpected_issue8_kuromoji() throws Exception {

		String projectName = "kuromoji";

		List<TestIntermediateAnalysisResult> result = runRTinProject(projectName);

		result = result.stream().filter(e -> e.isRotten() || e.isSmokeTest()).collect(Collectors.toList());
		assertEquals(2, result.size());
		assertTrue(result.get(0).isRotten());

	}

	@Test
	public void testRT1_RottenInTryCatch_issue9_Mycat_Server() throws Exception {

		String projectName = "Mycat-Server";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_RottenInTryCatch_issue_22_jboss_hw_ws() throws Exception {

		String projectName = "helloworld-ws";

		runRTinProject(projectName, "jboss-eap-quickstarts");

	}

	@Test
	@Ignore //
	public void testRT1_RottenInTryCatch_issue2_FXGL() throws Exception {

		String projectName = "FXGL";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_RottenInNoTest_issue15_fastdfsclientjava() throws Exception {

		String projectName = "fastdfs-client-java";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_RottenInTryCatch_issue2_GCViewer() throws Exception {

		String projectName = "GCViewer";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_NPE_issue17_wasabi() throws Exception {

		String projectName = "wasabi";

		List<TestIntermediateAnalysisResult> result = runRTinProject(projectName);

		result = result.stream().filter(e -> e.isRotten() || e.isSmokeTest()).collect(Collectors.toList());
		assertTrue(result.size() > 0);

	}

	@Test
	public void testRT1_NPE_issue4_takes() throws Exception {

		String projectName = "takes";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_NPE_issue15_rakam() throws Exception {

		String projectName = "rakam";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_NPE_issue21_Sentinel() throws Exception {

		String projectName = "sentinel";

		List<TestIntermediateAnalysisResult> result = runRTinProject(projectName);

		result = result.stream().filter(e -> e.isRotten() || e.isSmokeTest()).collect(Collectors.toList());
		assertTrue(result.size() > 0);

	}

	@Test
	public void testRT1_RottenExpected_issue_8_kuromoji_detailed_missed() throws Exception {

		File location = new File("/Users/matias/develop/rt-research/clonedrepos//kuromoji/");

		AstorMain main1 = new AstorMain();

		CommandSummary cs = RtTest.getCommand(location, null, "kuromoji", "kuromoji");
		cs.append("-parameters", "skipanalysis:true");
		main1.execute(cs.flat());
		RtEngine etEn = (RtEngine) main1.getEngine();

		RuntimeInformation ri = etEn.computeDynamicInformation();
		assertNotNull(ri);

		List<TestIntermediateAnalysisResult> resultByTest = etEn.getResultByTest();

		System.out.println(resultByTest);

		String classNameOfTest = "com.atilika.kuromoji.trie.PatriciaTrieTest";
		CtClass aTestModelCtClass = MutationSupporter.getFactory().Class().get(classNameOfTest);

		assertNotNull(aTestModelCtClass);

		TestIntermediateAnalysisResult tresult = etEn.processTest("testNull", classNameOfTest, aTestModelCtClass, ri);

		assertNotNull(tresult);

		assertTrue(tresult.isRotten());

		TestAnalysisResult generateFinalResult = tresult.generateFinalResult();
		assertTrue(generateFinalResult.getFullRotten().size() == 0);
		assertTrue(generateFinalResult.missedFail.size() > 0);
		assertEquals(1, generateFinalResult.missedFail.size());
		assertFalse(generateFinalResult.missedFail.get(0).isFp());

		etEn.resultByTest.add(tresult);

	}

	@Test
	public void testRT1_RottenExpected_issue_13_kuromoji_detailed_wrong_missed() throws Exception {

		File location = new File("/Users/matias/develop/rt-research/clonedrepos//kuromoji/");

		AstorMain main1 = new AstorMain();

		CommandSummary cs = RtTest.getCommand(location, null, "kuromoji", "kuromoji");
		cs.append("-parameters", "skipanalysis:true");
		main1.execute(cs.flat());
		RtEngine etEn = (RtEngine) main1.getEngine();

		RuntimeInformation ri = etEn.computeDynamicInformation();
		assertNotNull(ri);

		List<TestIntermediateAnalysisResult> resultByTest = etEn.getResultByTest();

		System.out.println(resultByTest);

		String classNameOfTest = "com.atilika.kuromoji.trie.PatriciaTrieTest";
		CtClass aTestModelCtClass = MutationSupporter.getFactory().Class().get(classNameOfTest);

		assertNotNull(aTestModelCtClass);

		TestIntermediateAnalysisResult tresult = etEn.processTest("testMultiThreadedTrie", classNameOfTest,
				aTestModelCtClass, ri);

		assertNotNull(tresult);

		assertFalse(tresult.isRotten());

		TestAnalysisResult generateFinalResult = tresult.generateFinalResult();
		assertTrue(generateFinalResult.getFullRotten().size() == 0);
		assertTrue(generateFinalResult.missedFail.size() == 0);
		assertEquals(0, generateFinalResult.missedFail.size());
		// assertTrue(generateFinalResult.missed.get(0).isFp());

		etEn.resultByTest.add(tresult);

	}

	@Test
	public void testRT1_RottenInTryCatch_issue9_Mycat_Server_detailed() throws Exception {

		File location = new File("/Users/matias/develop/rt-research/clonedrepos//Mycat-Server/");

		AstorMain main1 = new AstorMain();

		CommandSummary cs = RtTest.getCommand(location, null, "Mycat-Server", "Mycat-Server");
		cs.append("-parameters", "skipanalysis:true");
		main1.execute(cs.flat());
		RtEngine etEn = (RtEngine) main1.getEngine();

		RuntimeInformation ri = etEn.computeDynamicInformation();
		assertNotNull(ri);

		List<TestIntermediateAnalysisResult> resultByTest = etEn.getResultByTest();

		System.out.println(resultByTest);

		String classNameOfTest = "io.mycat.util.SmallSetTest";
		CtClass aTestModelCtClass = MutationSupporter.getFactory().Class().get(classNameOfTest);

		assertNotNull(aTestModelCtClass);

		TestIntermediateAnalysisResult tresult = etEn.processTest("testSet", classNameOfTest, aTestModelCtClass, ri);

		assertNotNull(tresult);

		assertTrue(tresult.isRotten());

		TestAnalysisResult generateFinalResult = tresult.generateFinalResult();
		assertTrue(generateFinalResult.getFullRotten().size() == 0);
		assertTrue(generateFinalResult.missedFail.size() > 0);
		assertEquals(1, generateFinalResult.missedFail.size());
		assertTrue(generateFinalResult.missedFail.get(0).isFp());

		etEn.resultByTest.add(tresult);

	}

	//
	public List<TestIntermediateAnalysisResult> runRTinProject(String projectName) throws Exception {
		return runRTinProject(projectName, "");
	}

	public List<TestIntermediateAnalysisResult> runRTinProject(String projectName, String root) throws Exception {
		File location = new File(
				"/Users/matias/develop/rt-research/clonedrepos/" + root + File.separator + projectName);

		RtEngine etEn = RtTest.detectRt(location, null, projectName, null);

		List<TestIntermediateAnalysisResult> resultByTest = etEn.getResultByTest();

		System.out.println(resultByTest);

		boolean hasrt = resultByTest.stream().anyMatch(e -> e.isRotten());
		return resultByTest;
	}

	@Test
	public void testRT1_FalsePositive_Smoke_issue_23() throws Exception {

		File location = new File("/Users/matias/develop/rt-research/clonedrepos/red5-server/");

		AstorMain main1 = new AstorMain();

		CommandSummary cs = RtTest.getCommand(location, null, "red5-server", "");
		cs.append("-parameters", "skipanalysis:false");
		main1.execute(cs.flat());
		RtEngine etEn = (RtEngine) main1.getEngine();

		RuntimeInformation ri = etEn.computeDynamicInformation();
		assertNotNull(ri);

		List<TestIntermediateAnalysisResult> resultByTest = etEn.getResultByTest();

		System.out.println(resultByTest);

		String classNameOfTest = "org.red5.server.service.CuePointInjectionTest";
		CtClass aTestModelCtClass = MutationSupporter.getFactory().Class().get(classNameOfTest);

		assertNotNull(aTestModelCtClass);
		TestIntermediateAnalysisResult tresult = etEn.processTest("testCuePointOrder", classNameOfTest,
				aTestModelCtClass, ri);

		assertNotNull(tresult);

		// assertTrue(tresult.isRotten());

		TestAnalysisResult generateFinalResult = tresult.generateFinalResult();
		assertEquals(0, generateFinalResult.getFullRotten().size());
		assertEquals(0, generateFinalResult.missedFail.size());
		assertEquals(1, generateFinalResult.redundantAssertion.size());
		assertFalse(generateFinalResult.smokeTest);

		// assertFalse(generateFinalResult.getOtherMethodInvocations().stream()
		// .filter(e ->
		// e.toString().toLowerCase().contains("assert")).findAny().isPresent());

		etEn.resultByTest.add(tresult);

	}

	@Test
	public void testRT1_FalsePositive_Smoke_issue_23_reflectasm() throws Exception {

		File location = new File("/Users/matias/develop/rt-research/clonedrepos/reflectasm/");

		AstorMain main1 = new AstorMain();

		CommandSummary cs = RtTest.getCommand(location, null, "reflectasm", "");
		cs.append("-parameters", "skipanalysis:false");
		main1.execute(cs.flat());
		RtEngine etEn = (RtEngine) main1.getEngine();

		RuntimeInformation ri = etEn.computeDynamicInformation();
		assertNotNull(ri);

		List<TestIntermediateAnalysisResult> resultByTest = etEn.getResultByTest();

		System.out.println(resultByTest);

		String classNameOfTest = "com.esotericsoftware.reflectasm.MethodAccessTest";
		CtClass aTestModelCtClass = MutationSupporter.getFactory().Class().get(classNameOfTest);

		assertNotNull(aTestModelCtClass);
		TestIntermediateAnalysisResult tresult = etEn.processTest("testEmptyClass", classNameOfTest, aTestModelCtClass,
				ri);

		assertNotNull(tresult);

		// assertTrue(tresult.isRotten());
//TODO: add try detection
		TestAnalysisResult generateFinalResult = tresult.generateFinalResult();
		assertEquals(0, generateFinalResult.getFullRotten().size());
		assertEquals(0, generateFinalResult.missedFail.size());
		assertEquals(0, generateFinalResult.redundantAssertion.size());
		assertFalse(generateFinalResult.smokeTest);

		// assertFalse(generateFinalResult.getOtherMethodInvocations().stream()
		// .filter(e ->
		// e.toString().toLowerCase().contains("assert")).findAny().isPresent());

		etEn.resultByTest.add(tresult);

	}

	// xchange
	// google-cloud-java
	@Test
	public void testRT1_not_finish_in_grid_jero() throws Exception {

		File location = new File("/Users/matias/develop/rt-research/clonedrepos/jeromq/");

		AstorMain main1 = new AstorMain();

		CommandSummary cs = RtTest.getCommand(location, null, "jeromq", "");
		cs.append("-parameters", "skipanalysis:false");
		main1.execute(cs.flat());
		RtEngine etEn = (RtEngine) main1.getEngine();

		RuntimeInformation ri = etEn.computeDynamicInformation();
		assertNotNull(ri);

		List<TestIntermediateAnalysisResult> resultByTest = etEn.getResultByTest();

		System.out.println(resultByTest);

	}

	@Test
	public void testRT1_KARATE() throws Exception {

		String projectName = "karate";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_XChange() throws Exception {

		String projectName = "XChange";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_kilim_not_analyzed_on_grid() throws Exception {

		String projectName = "kilim";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_Discovery_not_analyzed_on_grid() throws Exception {
		// no test
		String projectName = "Discovery";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_quickstart_not_analyzed_on_grid() throws Exception {
		// maven compile fails
		String projectName = "quickstart";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_actframework_not_analyzed_on_grid() throws Exception {
		//
		String projectName = "actframework";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_AppiumTestDistribution_not_analyzed_on_grid() throws Exception {
		//
		String projectName = "AppiumTestDistribution";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_blueocean_plugin_not_analyzed_on_grid() throws Exception {
		//
		String projectName = "blueocean-plugin";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_FXGL_npe_module() throws Exception {
		//
		String projectName = "FXGL";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_closure_compiler_npe_module() throws Exception {
		//
		String projectName = "closure-compiler";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_eclipsejdtls_compiler_empty_source() throws Exception {
		//
		String projectName = "eclipse.jdt.ls";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_fabricsdkjava_compiler_npe_gzoltar() throws Exception {
		//
		String projectName = "fabric-sdk-java";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_fastdfsclientjava_compiler_no_testbin() throws Exception {
		//
		String projectName = "fastdfs-client-java";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_HanLP_compiler() throws Exception {
		//
		String projectName = "HanLP";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_ionjava_gzoltar() throws Exception {
		//
		String projectName = "ion-java";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_itchat4j_notest() throws Exception {
		//
		String projectName = "itchat4j";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_jeromqj_timeout() throws Exception {
		//
		String projectName = "jeromq";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_karate_nowOK() throws Exception {
		//
		String projectName = "karate";

		runRTinProject(projectName);

	}

	@Test
	public void testRT1_httpcomponentsclient_timeout() throws Exception {
		//
		String projectName = "httpcomponents-client";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_kilim_gzoltar() throws Exception {
		//
		String projectName = "kilim";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_light_task_scheduler() throws Exception {
		//
		String projectName = "light-task-scheduler";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_Minim_nojunit() throws Exception {
		//
		String projectName = "Minim";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_pahomqttjavat() throws Exception {
		//
		String projectName = "paho.mqtt.java";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_pippo() throws Exception {
		//
		String projectName = "pippo";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_rtree() throws Exception {
		//
		String projectName = "rtree";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_presto() throws Exception {
		//
		String projectName = "presto";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_rakam() throws Exception {
		//
		String projectName = "rakam";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_socket() throws Exception {
		//
		String projectName = "socket.io-client-java";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_springcloudcode_failingcasesingzoltar() throws Exception {
		//
		String projectName = "spring-cloud-code";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_servicecomb() throws Exception {
		//
		String projectName = "servicecomb-java-chassis";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_Discovery() throws Exception {
		//
		String projectName = "Discovery";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_actframework() throws Exception {
		//
		String projectName = "actframework";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_springbootquick_failingtests() throws Exception {
		//
		String projectName = "spring-boot-quick";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_lambda() throws Exception {
		//
		String projectName = "lambda";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_takes() throws Exception {
		//
		String projectName = "takes";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_spring_cloud_gateway() throws Exception {
		//
		String projectName = "spring-cloud-gateway";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_optaplanner() throws Exception {
		//
		String projectName = "optaplanner";

		runRTinProject(projectName);

	}

	@Test
	public void testxRT1_optaplanner_debug() throws Exception {
		//
		String projectName = "optaplanner";

		File location = new File("/Users/matias/develop/rt-research/clonedrepos/" + projectName);

		RtEngine etEn = RtTest.detectRtSkip(location, null, projectName, null);

		RuntimeInformation dynInf = etEn.computeDynamicInformation();

		// First case: not executed a method inv
		TestIntermediateAnalysisResult rottenTest0 = etEn.processSingleTest(dynInf,
				"org.optaplanner.core.impl.heuristic.selector.value.decorator.CachingValueSelectorTest",
				"originalSelectionCacheTypeSolver");

		assertNotNull(rottenTest0);

		assertTrue(rottenTest0.getClassificationHelperCall().getResultNotExecuted().isEmpty());

		assertTrue(rottenTest0.isRotten());

		TestAnalysisResult finalResult = rottenTest0.generateFinalResult();
		assertTrue(finalResult.fullRottenHelperAssert.isEmpty());
		assertTrue(finalResult.contextHelperAssertion.size() > 0);
		assertTrue(finalResult.fullRottenAssert.isEmpty());
		assertTrue(finalResult.fullRottenHelperCall.isEmpty());
		assertTrue(finalResult.skip.isEmpty());

		assertTrue(finalResult.contextHelperAssertion.get(0).isFp());

		etEn.atEnd();

	}

	@Test
	public void testxRT1_streamex() throws Exception {
		//
		String projectName = "streamex";

		runRTinProject(projectName);

	}

	// SHOULD be
	// https://github.com/amaembo/streamex/blob/1190608bda70885f55ec791ebc0e76f89006db6a/src/test/java/one/util/streamex/BaseStreamExTest.java#L106

	@Test
	public void testRT1_ionjava_direct() throws Exception {
		//
		String projectName = "ion-java";

		AstorMain main1 = new AstorMain();
		File location = new File("/Users/matias/develop/rt-research/clonedrepos/" + projectName);

		CommandSummary cs = RtTest.getCommand(location, null, projectName, null);
		cs.command.put("-dependencies",
				"/Users/matias/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:/Users/matias/.m2/repository/junit/junit/4.12/junit-4.12.jar");
		cs.command.remove("-autoconfigure");
		cs.command.put("-srcjavafolder", "/src/main/java/");
		cs.command.put("-srctestfolder", "/src/test/java/");
		cs.command.put("-binjavafolder", "/target/classes/");
		cs.command.put("-bintestfolder", "/target/test-classes/");

		main1.execute(cs.flat());

		RtEngine etEn = (RtEngine) main1.getEngine();

		// runRTinProject(projectName);

	}

	String[] missing28 = new String[] { "blueocean-plugin", "closure-compiler", "docker-maven-plugin", "eclipse.jdt.ls",
			"fabric-sdk-java", "fastdfs-client-java", "framework", "FXGL", "google-cloud-java", "HanLP", "ion-java",
			"itchat4j", "karate", "kilim", "Minim", "Mycat-Server", "paho.mqtt.java", "presto", "rakam",
			"socket.io-client-java", "spring-cloud-code", "light-task-scheduler", "servicecomb-java-chassis",
			"httpcomponents-client", "jeromq", "pippo" };

	String[] missing7 = new String[] { "polyglot-maven" };

	String[] missing7old = new String[] { "spring-cloud-code", "polyglot-maven", "jeromq", "socket.io-client-java",
			"pippo", "actframework", "spring-cloud-gateway" };

	@Test
	public void testRT1_allMissing28() throws Exception {

		for (String projectName : missing7) {
			System.out.println("running " + projectName);
			try {
				runRTinProject(projectName);
			} catch (Throwable e) {
				System.out.println(" error with " + projectName);
				e.printStackTrace();
			}
		}

	}

}
