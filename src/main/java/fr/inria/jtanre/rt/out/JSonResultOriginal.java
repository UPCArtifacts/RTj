package fr.inria.jtanre.rt.out;

import static fr.inria.jtanre.rt.out.TestCategories.FULL_ROTTEN_TEST;
import static fr.inria.jtanre.rt.out.TestCategories.FULL_ROTTEN_TEST_ASSERTIONS;
import static fr.inria.jtanre.rt.out.TestCategories.FULL_ROTTEN_TEST_HELPERS_ASSERTION;
import static fr.inria.jtanre.rt.out.TestCategories.FULL_ROTTEN_TEST_HELPERS_CALL;
import static fr.inria.jtanre.rt.out.TestCategories.ROTTEN_CONTEXT_DEP;
import static fr.inria.jtanre.rt.out.TestCategories.ROTTEN_CONTEXT_DEP_ASSERTIONS;
import static fr.inria.jtanre.rt.out.TestCategories.ROTTEN_CONTEXT_DEP_HELPERS_ASSERTION;
import static fr.inria.jtanre.rt.out.TestCategories.ROTTEN_CONTEXT_DEP_HELPERS_CALL;
import static fr.inria.jtanre.rt.out.TestCategories.ROTTEN_SKIP;
import static fr.inria.jtanre.rt.out.TestCategories.SMOKE_TEST;
import static fr.inria.jtanre.rt.out.TestCategories.TEST_HAS_CONTROL_FLOW_STMT;
import static fr.inria.jtanre.rt.out.TestCategories.TEST_HAS_HELPER_CALL;
import static fr.inria.jtanre.rt.out.TestCategories.TEST_MISSED_FAIL;
import static fr.inria.jtanre.rt.out.TestCategories.TEST_WITH_EXCEPTION;
import static fr.inria.jtanre.rt.out.TestCategories.TEST_WITH_REDUNDANT_ASSERTION;
import static fr.inria.jtanre.rt.out.TestCategories.TYPE_ROTTEN;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.jtanre.rt.RtEngine;
import fr.inria.jtanre.rt.core.GenericTestAnalysisResults;
import fr.inria.jtanre.rt.core.TestAnalysisResult;
import fr.inria.jtanre.rt.core.TestIntermediateAnalysisResult;
import fr.inria.jtanre.rt.elements.AsAssertion;
import fr.inria.jtanre.rt.elements.Helper;
import fr.inria.jtanre.rt.elements.Skip;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

/**
 * 
 * @author Matias Martinez
 *
 */
public class JSonResultOriginal implements RtOutput {

	protected static Logger log = Logger.getLogger(Thread.currentThread().getName());

	ProjectRepairFacade projectFacade = null;

	public JsonObject toJsonError(String name, ProjectRepairFacade projectFacade, Exception e) {

		JsonObject root = new JsonObject();
		root.addProperty("project", projectFacade.getProperties().getFixid());
		JsonObject summary = new JsonObject();
		root.add("project", summary);
		String location = ConfigurationProperties.getProperty("location");
		String commitid = executeCommand(location, "git rev-parse HEAD");

		summary.addProperty("commitid", commitid);

		root.addProperty("error", e.getMessage());
		JsonArray testsArray = new JsonArray();
		root.add("tests", testsArray);

		return root;
	}

	public JsonObject toJson(RtEngine engine, String name, ProjectRepairFacade projectFacade,
			List<TestIntermediateAnalysisResult> resultByTest) {

		this.projectFacade = projectFacade;

		JsonObject root = new JsonObject();
		root.addProperty("identifier", projectFacade.getProperties().getFixid());
		JsonObject summary = new JsonObject();
		root.add("project", summary);
		String projectName = name;
		String location = ConfigurationProperties.getProperty("location");
		String commitid = executeCommand(location, "git rev-parse HEAD");
		String branch = executeCommand(location, "git rev-parse --abbrev-ref HEAD");
		String remote = executeCommand(location, "git config --get remote.origin.url");
		String projectsubfolder = ConfigurationProperties.getProperty("projectsubfolder");
		summary.addProperty("commitid", commitid);

		int nrRtest = 0, nrRtAssertion = 0, nrRtHelperCall = 0, nrRttHelperAssert = 0, nrSkip = 0, nrAllMissed = 0,
				nrAllRedundant = 0, nrSmokeTest = 0, nrRtFull = 0, nrTestWithControlStruct = 0, nrTestWithHelper = 0,
				nrWithExceptions = 0;

		JsonArray testsArray = new JsonArray();
		root.add("tests", testsArray);
		Set<String> rTestclasses = new HashSet<>();

		boolean analyzeSmoke = ConfigurationProperties.getPropertyBool("include_smoke");
		boolean analyzeRedundant = ConfigurationProperties.getPropertyBool("include_redundant_assertions");
		boolean analyzeExceptions = ConfigurationProperties.getPropertyBool("include_exceptions");

		JsonObject testInfo = new JsonObject();
		root.add("test", testInfo);

		StringBuffer sb = new StringBuffer();

		List<String> cacheAnayzed = new ArrayList();

		for (GenericTestAnalysisResults gr : resultByTest) {

			TestIntermediateAnalysisResult tr = (TestIntermediateAnalysisResult) gr;
			TestAnalysisResult rottenResults = tr.generateFinalResult();

			JsonObject testjson = new JsonObject();
			JsonArray typesRottens = new JsonArray();
			Set<String> uniquesTypesRottern = new HashSet();
			testjson.add("rotten_types_summary", typesRottens);

			JsonArray summaryRottens = new JsonArray();
			testjson.add("rotten_info", summaryRottens);
			testjson.addProperty("test_class", tr.getNameOfTestClass());
			String testMethodFromClass = tr.getTestMethodFromClass();
			testjson.addProperty("test_name", testMethodFromClass);
			testjson.addProperty("has_expects_exception", (tr.getExpectException().size() > 0) ? "true" : "false");
			testjson.addProperty("executed_only_assume", (tr.isOnlyAssumeExecuted()) ? "true" : "false");
			testjson.addProperty("nr_assume", tr.getAllAssumesFromTest().size());

			writeJsonLink(commitid, branch, remote, projectsubfolder, tr.getTestMethodModel(), testjson);

			boolean onerotten = false;

			boolean hasControlFlow = tr.hasControlFlow();
			nrTestWithControlStruct += (hasControlFlow) ? 1 : 0;
			testjson.addProperty("has_control_flow", hasControlFlow);

			boolean hasHelperCall = tr.hasHelperCall();
			nrTestWithHelper += (hasHelperCall) ? 1 : 0;

			boolean hasFailInvocation = tr.hasFailInvocation();
			testjson.addProperty("has_fail_invocation", hasFailInvocation);

			boolean hasTryCatch = tr.hasTryCatch();
			testjson.addProperty("has_try_catch", hasTryCatch);

			int nrCasesNotExecuted = 0;

			//
			if (!rottenResults.missedFail.isEmpty() && !cacheAnayzed.contains(testMethodFromClass + TEST_MISSED_FAIL)) {
				for (AsAssertion missedInv : rottenResults.missedFail) {
					JsonObject missedJson = new JsonObject();
					missedJson.addProperty("code_assertion", missedInv.toString().toString());
					missedJson.addProperty("line_assertion", getPosition(missedInv.getCtAssertion()));
					missedJson.addProperty("path_assertion",
							getRelativePath(missedInv.getCtAssertion(), projectFacade));
					writeJsonLink(commitid, branch, remote, projectsubfolder, missedInv.getCtAssertion(), missedJson);
					onerotten = true;
					summaryRottens.add(missedJson);
					missedJson.addProperty(TYPE_ROTTEN, TEST_MISSED_FAIL);
					uniquesTypesRottern.add(TEST_MISSED_FAIL);
					cacheAnayzed.add(testMethodFromClass + TEST_MISSED_FAIL);
				}
				nrAllMissed++;
			}

			//
			if (!rottenResults.skip.isEmpty() && !cacheAnayzed.contains(testMethodFromClass + ROTTEN_SKIP)) {

				for (Skip iSkip : rottenResults.skip) {
					CtReturn skip = iSkip.getExecutedReturn();
					JsonObject singleSkip = new JsonObject();
					singleSkip.addProperty("code", skip.toString().toString());
					singleSkip.addProperty("line", getPosition(skip));
					// singleSkip.add("parent_types", getParentTypes(skip));
					onerotten = true;
					summaryRottens.add(singleSkip);
					singleSkip.addProperty(TYPE_ROTTEN, ROTTEN_SKIP);
					writeJsonLink(commitid, branch, remote, projectsubfolder, skip, singleSkip);
					uniquesTypesRottern.add(ROTTEN_SKIP);
					cacheAnayzed.add(testMethodFromClass + ROTTEN_SKIP);
				}
				nrSkip++;
			} else if ((!rottenResults.contextAssertion.isEmpty() || !rottenResults.contextHelperCall.isEmpty()
					|| !rottenResults.contextHelperAssertion.isEmpty())
					&& !cacheAnayzed.contains(testMethodFromClass + ROTTEN_CONTEXT_DEP)) {

				// Here the complex:

				cacheAnayzed.add(testMethodFromClass + ROTTEN_CONTEXT_DEP);

				// Asserts

				nrCasesNotExecuted = add_ASSERTIONS(projectFacade, commitid, branch, remote, projectsubfolder, tr,
						summaryRottens, uniquesTypesRottern, rottenResults.contextAssertion,
						ROTTEN_CONTEXT_DEP_ASSERTIONS);
				onerotten = onerotten || (nrCasesNotExecuted > 0);
				nrRtAssertion += (nrCasesNotExecuted > 0) ? 1 : 0;
				//

				nrCasesNotExecuted = add_HELPERS_CALL(commitid, branch, remote, projectsubfolder, tr, summaryRottens,
						uniquesTypesRottern, rottenResults.contextHelperCall, ROTTEN_CONTEXT_DEP_HELPERS_CALL);
				onerotten = onerotten || (nrCasesNotExecuted > 0);
				nrRtHelperCall += (nrCasesNotExecuted > 0) ? 1 : 0;

				//

				nrCasesNotExecuted = add_HELPERS_ASSERTION(commitid, branch, remote, projectsubfolder, tr,
						summaryRottens, uniquesTypesRottern, rottenResults.contextHelperAssertion,
						ROTTEN_CONTEXT_DEP_ASSERTIONS);
				onerotten = onerotten || (nrCasesNotExecuted > 0);
				nrRttHelperAssert += (nrCasesNotExecuted > 0) ? 1 : 0;

			} else if ((!rottenResults.fullRottenAssert.isEmpty() || !rottenResults.fullRottenHelperCall.isEmpty()
					|| !rottenResults.fullRottenHelperAssert.isEmpty())
					&& !cacheAnayzed.contains(testMethodFromClass + FULL_ROTTEN_TEST)) {
				/// ------Now the full-----

				cacheAnayzed.add(testMethodFromClass + FULL_ROTTEN_TEST);

				nrCasesNotExecuted = add_ASSERTIONS(projectFacade, commitid, branch, remote, projectsubfolder, tr,
						summaryRottens, uniquesTypesRottern, rottenResults.fullRottenAssert,
						FULL_ROTTEN_TEST_ASSERTIONS);
				onerotten = onerotten || (nrCasesNotExecuted > 0);
				nrRtFull += (nrCasesNotExecuted > 0) ? 1 : 0;
				//

				nrCasesNotExecuted = add_HELPERS_CALL(commitid, branch, remote, projectsubfolder, tr, summaryRottens,
						uniquesTypesRottern, rottenResults.fullRottenHelperCall, FULL_ROTTEN_TEST_HELPERS_CALL);
				onerotten = onerotten || (nrCasesNotExecuted > 0);
				nrRtFull += (nrCasesNotExecuted > 0) ? 1 : 0;

				//
				nrCasesNotExecuted = add_HELPERS_ASSERTION(commitid, branch, remote, projectsubfolder, tr,
						summaryRottens, uniquesTypesRottern, rottenResults.fullRottenHelperAssert,
						FULL_ROTTEN_TEST_HELPERS_ASSERTION);
				onerotten = onerotten || (nrCasesNotExecuted > 0);
				nrRtFull += (nrCasesNotExecuted > 0) ? 1 : 0;

			}
			// Now the additional cases
			//
			if (analyzeRedundant) {
				if (!rottenResults.redundantAssertion.isEmpty()
						&& !cacheAnayzed.contains(testMethodFromClass + TEST_WITH_REDUNDANT_ASSERTION)) {
					for (AsAssertion missedInv : rottenResults.redundantAssertion) {
						JsonObject missedJson = new JsonObject();
						missedJson.addProperty("code_assertion", missedInv.toString().toString());
						missedJson.addProperty("line_assertion", getPosition(missedInv.getCtAssertion()));
						missedJson.addProperty("path_assertion",
								getRelativePath(missedInv.getCtAssertion(), projectFacade));
						writeJsonLink(commitid, branch, remote, projectsubfolder, missedInv.getCtAssertion(),
								missedJson);
						onerotten = true;
						summaryRottens.add(missedJson);
						missedJson.addProperty(TYPE_ROTTEN, TEST_WITH_REDUNDANT_ASSERTION);
						uniquesTypesRottern.add(TEST_WITH_REDUNDANT_ASSERTION);
						cacheAnayzed.add(testMethodFromClass + TEST_WITH_REDUNDANT_ASSERTION);
					}
					nrAllRedundant++;
				}
			}
			boolean withexception = false;
			if (analyzeExceptions) {
				if (tr.isExceptionExpected() && tr.testElementsNotPresentInTest()
						&& !cacheAnayzed.contains(testMethodFromClass + TEST_WITH_EXCEPTION)) {
					JsonObject testWithException = new JsonObject();
					summaryRottens.add(testWithException);
					testWithException.addProperty(TYPE_ROTTEN, TEST_WITH_EXCEPTION);

					JsonArray expEx = new JsonArray();
					for (String ee : tr.getExpectException()) {
						expEx.add(ee);
					}
					JsonArray failsAr = new JsonArray();
					//
					testWithException.add("expected_exception", expEx);
					testWithException.add("fails", failsAr);

					for (CtInvocation inv : tr.getAllFailsFromTest()) {

						JsonObject failJson = new JsonObject();
						failJson.addProperty("code_assertion", inv.toString().toString());
						failJson.addProperty("line_assertion", getPosition(inv));
						failJson.addProperty("path_assertion", getRelativePath(inv, projectFacade));
						writeJsonLink(commitid, branch, remote, projectsubfolder, inv, failJson);
						onerotten = true;
						failsAr.add(failJson);
					}

					withexception = true;
					nrWithExceptions++;
					uniquesTypesRottern.add(TEST_WITH_EXCEPTION);
					cacheAnayzed.add(testMethodFromClass + TEST_WITH_EXCEPTION);
				}
			}
			if (analyzeSmoke) {
				if (tr.isSmokeTest() && tr.getExpectException().isEmpty()
						&& tr.getAllExpectedExceptionFromTest().isEmpty()
						&& cacheAnayzed.contains(testMethodFromClass + SMOKE_TEST)) {

					List<CtInvocation> allAssertionsFromTest = rottenResults.getOtherMethodInvocations();

					testsArray.add(testjson);
					rTestclasses.add(tr.getNameOfTestClass());
					nrSmokeTest += 1;
					JsonObject smokeTest = new JsonObject();
					smokeTest.addProperty(TYPE_ROTTEN, SMOKE_TEST);

					JsonArray missrarray = new JsonArray();
					smokeTest.add("other_method_invocation", missrarray);
					for (CtInvocation otherinv : allAssertionsFromTest) {
						missrarray.add(createMethodSignature(otherinv));
					}

					summaryRottens.add(smokeTest);

					uniquesTypesRottern.add(SMOKE_TEST);
					cacheAnayzed.add(testMethodFromClass + SMOKE_TEST);

				}
			}

			/// Dont include smoke
			if (onerotten) {
				testsArray.add(testjson);
				nrRtest++;
				rTestclasses.add(tr.getNameOfTestClass());
			} else if (withexception) {
				testsArray.add(testjson);
				rTestclasses.add(tr.getNameOfTestClass());
			}

			// Some stats
			if (testjson != null) {
				testjson.addProperty(TEST_HAS_CONTROL_FLOW_STMT, hasControlFlow);
				testjson.addProperty(TEST_HAS_HELPER_CALL, hasHelperCall);
			}
			// We put the the types found in "types"
			for (String types : uniquesTypesRottern) {
				typesRottens.add(types);
			}
		}

		summary.addProperty("time_tests_exec_sec", engine.getExecutionTimeTests() / 1000);
		summary.addProperty("time_analysis_exec_sec", engine.getExecutionTimeAnalyzers() / 1000);
		summary.addProperty("name", projectName);
		summary.addProperty("remote", remote);
		summary.addProperty("local_location", location);
		summary.addProperty("nr_test_with_control_flow_stmt", nrTestWithControlStruct);
		summary.addProperty("nr_test_with_helper", nrTestWithHelper);
		summary.addProperty("nr_all_test_cases", resultByTest.size());
		summary.addProperty("nr_all_test_classes", projectFacade.getProperties().getRegressionTestCases().size());
		summary.addProperty("nr_rotten_test_units", nrRtest);

		Collection<CtPackage> packages = MutationSupporter.getFactory().Package().getAll();

		summary.addProperty("nr_packages", packages.size());
		addTestCases(testInfo, resultByTest);
		addTestClasses(testInfo, projectFacade.getProperties().getRegressionTestCases());

		Collection<CtType<?>> typesspoon = MutationSupporter.getFactory().Type().getAll();
		summary.addProperty("nr_classes", typesspoon.size());

		summary.addProperty("nr_" + ROTTEN_CONTEXT_DEP_ASSERTIONS, nrRtAssertion);
		summary.addProperty("nr_" + ROTTEN_CONTEXT_DEP_HELPERS_CALL, nrRtHelperCall);
		summary.addProperty("nr_" + ROTTEN_CONTEXT_DEP_HELPERS_ASSERTION, nrRttHelperAssert);
		summary.addProperty("nr_" + ROTTEN_SKIP, nrSkip);
		summary.addProperty("nr_" + TEST_MISSED_FAIL, nrAllMissed);
		summary.addProperty("nr_" + FULL_ROTTEN_TEST, nrRtFull);

		if (analyzeSmoke)
			summary.addProperty("nr_" + SMOKE_TEST, nrSmokeTest);

		if (analyzeExceptions)
			summary.addProperty("nr_" + TEST_WITH_EXCEPTION, nrWithExceptions);

		if (analyzeRedundant)
			summary.addProperty("nr_" + TEST_WITH_REDUNDANT_ASSERTION, nrAllRedundant);

		return root;
	}

	private void addTestClasses(JsonObject summary, List<String> regressionTestCases) {
		JsonArray tc = new JsonArray();
		summary.add("testclasses", tc);

		for (String test : regressionTestCases) {
			tc.add(test);
		}
	}

	private void addTestCases(JsonObject summary, List<TestIntermediateAnalysisResult> resultByTest) {
		JsonArray tc = new JsonArray();
		summary.add("testcases", tc);

		for (TestIntermediateAnalysisResult testIntermediateAnalysisResult : resultByTest) {
			tc.add(testIntermediateAnalysisResult.getNameOfTestClass() + "_"
					+ testIntermediateAnalysisResult.getTestMethodFromClass());
		}

	}

	public int getPosition(CtElement inv) {
		try {
			return inv.getPosition().getLine();
		} catch (Exception e) {
			log.error("Error getting position of element");
			e.printStackTrace();
			return -1;
		}
	}

	public int add_ASSERTIONS(ProjectRepairFacade projectFacade, String commitid, String branch, String remote,
			String projectsubfolder, TestIntermediateAnalysisResult tr, JsonArray summaryRottens,
			Set<String> uniquesTypesRottern, List<AsAssertion> notExecutedAssert,
			String ROTTEN_CONTEXT_DEP_ASSERTIONS) {
		int nrRtAssertion = 0;
		if (!notExecutedAssert.isEmpty()) {

			log.debug("-- Test  " + tr.getNameOfTestClass() + ": " + tr.getTestMethodFromClass());

			for (AsAssertion assertion : notExecutedAssert) {
				CtInvocation anInvocation = assertion.getCtAssertion();
				log.debug("-R-Assertion:-> " + anInvocation);
				JsonObject jsonsingleAssertion = new JsonObject();
				jsonsingleAssertion.addProperty("code", anInvocation.toString());
				jsonsingleAssertion.addProperty("line", getPosition(anInvocation));
				jsonsingleAssertion.addProperty("path", getRelativePath(anInvocation, projectFacade));
				jsonsingleAssertion.addProperty("other_branch_with_assert_executed", assertion.isFp());

				writeJsonLink(commitid, branch, remote, projectsubfolder, anInvocation, jsonsingleAssertion);
				summaryRottens.add(jsonsingleAssertion);
				jsonsingleAssertion.addProperty(TYPE_ROTTEN, ROTTEN_CONTEXT_DEP_ASSERTIONS);
				// jsonsingleAssertion.add("parent_types", getParentTypes(anInvocation));
				nrRtAssertion++;

				uniquesTypesRottern.add(ROTTEN_CONTEXT_DEP_ASSERTIONS);

			}
		}
		return nrRtAssertion;
	}

	public int add_HELPERS_ASSERTION(String commitid, String branch, String remote, String projectsubfolder,
			TestIntermediateAnalysisResult tr, JsonArray summaryRottens, Set<String> uniquesTypesRottern,
			List<Helper> notExecutedHelper, String ROTTEN_CONTEXT_DEP_HELPERS_ASSERTION) {
		int nrRttHelperAssert = 0;
		if (!notExecutedHelper.isEmpty()) {
			log.debug("-R Helper assertion- " + tr.getNameOfTestClass() + ": " + tr.getTestMethodFromClass());

			List<JsonObject> result = helperToJson(notExecutedHelper,
					tr.getClassificationHelperCall().getResultNotExecuted(), commitid, branch, remote, projectsubfolder,
					false);

			if (!result.isEmpty()) {
				for (JsonObject jsonObject : result) {
					summaryRottens.add(jsonObject);
					jsonObject.addProperty(TYPE_ROTTEN, ROTTEN_CONTEXT_DEP_HELPERS_ASSERTION);

				}
				uniquesTypesRottern.add(ROTTEN_CONTEXT_DEP_HELPERS_ASSERTION);
			}

			nrRttHelperAssert += notExecutedHelper.size();
		}
		return nrRttHelperAssert;
	}

	public int add_HELPERS_CALL(String commitid, String branch, String remote, String projectsubfolder,
			TestIntermediateAnalysisResult tr, JsonArray summaryRottens, Set<String> uniquesTypesRottern,
			List<Helper> notExecutedHelperInvoc, String ROTTEN_CONTEXT_DEP_HELPERS_CALL) {
		int nrRtHelperCall = 0;
		if (!notExecutedHelperInvoc.isEmpty()) {
			System.out.println("-- R Helper call  " + tr.getNameOfTestClass() + ": " + tr.getTestMethodFromClass());

			List<JsonObject> result = helperToJson(notExecutedHelperInvoc, Lists.newArrayList(), commitid, branch,
					remote, projectsubfolder, true);

			if (!result.isEmpty()) {

				for (JsonObject jsonObject : result) {
					summaryRottens.add(jsonObject);
					jsonObject.addProperty(TYPE_ROTTEN, ROTTEN_CONTEXT_DEP_HELPERS_CALL);
				}
				uniquesTypesRottern.add(ROTTEN_CONTEXT_DEP_HELPERS_CALL);
			}
			nrRtHelperCall += notExecutedHelperInvoc.size();
		}
		return nrRtHelperCall;
	}

	private String executeCommand(String location, String command) {

		log.debug("Running command  " + command + " at " + location);
		ProcessBuilder builder = new ProcessBuilder();

		builder.command(command.split(" "));

		builder.directory(new File(location));

		try {

			Process process = builder.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String content = "";
			String line;
			while ((line = reader.readLine()) != null) {
				content += line + "\n";
			}
			log.info("Command result " + content);
			return content.trim();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private JsonArray getParentTypes(CtElement anElement) {
		JsonArray parentArray = new JsonArray();
		CtElement parent = anElement.getParent();
		while (parent != null) {
			// removing the prefix "Ct" and suffix "Impl"
			parentArray.add(cleanTypeName(parent.getClass().getSimpleName()));
			parent = parent.getParent();
			// we discard parents from package
			if (parent instanceof CtPackage) {
				break;
			}
		}

		return parentArray;
	}

	// private boolean isFail(CtInvocation targetInvocation) {
	// return isInvWithName(targetInvocation, "fail");
	// }

	private boolean hasFail(List<CtInvocation> allAssertionsFromTest) {
		for (CtInvocation ctInvocation : allAssertionsFromTest) {
			// if (isFail(ctInvocation)) {
			return true;
			// }
		}
		return false;
	}

	public void writeJsonLink(String commitid, String branch, String remote, String projectsubfolder,
			CtElement anInvocation, JsonObject singleAssertion) {
		if (remote != null && branch != null && commitid != null) {
			singleAssertion.addProperty("github_link", remote.replace(".git", "")
					// "https://github.com/" + projectname
					+ "/tree/" + commitid// branch
					+ ((projectsubfolder != null) ? "/" + projectsubfolder : "") + "/"
					+ getRelativePath(anInvocation, this.projectFacade) + "#L" + getPosition(anInvocation));
		}
	}

	public List<JsonObject> helperToJson(List<Helper> notExecutedHelper, List<Helper> others, String commitid,
			String branch, String remote, String projectsubfolder, boolean isCall) {

		List<JsonObject> result = new ArrayList();

		for (Helper anHelper : notExecutedHelper) {

			if (others.contains(anHelper)) {
				continue;
			}
			log.debug("-Helper-> " + anHelper);
			CtInvocation ctAssertion = anHelper.getAssertion().getCtAssertion();
			JsonObject jsonsingleHelper = new JsonObject();

			JsonObject assertionjson = getJsonElement(commitid, branch, remote, projectsubfolder, ctAssertion);
			jsonsingleHelper.add("assertion", assertionjson);
			jsonsingleHelper.addProperty("other_branch_with_assert_executed", anHelper.isFp());
			JsonArray callsarray = new JsonArray();
			for (CtInvocation call : anHelper.getCalls()) {
				callsarray.add(getJsonElement(commitid, branch, remote, projectsubfolder, call));
			}
			jsonsingleHelper.add("calls", callsarray);
			jsonsingleHelper.addProperty("distance_calls", callsarray.size());
			jsonsingleHelper.addProperty("distance_hierarchy", anHelper.distance);

			if (isCall) {

				if (anHelper.getCalls().size() > 0)
					writeJsonLink(commitid, branch, remote, projectsubfolder, anHelper.getCalls().get(0),
							jsonsingleHelper);

			} else {

				writeJsonLink(commitid, branch, remote, projectsubfolder, ctAssertion, jsonsingleHelper);
			}

			result.add(jsonsingleHelper);

		}
		return result;
	}

	public JsonObject getJsonElement(String commitid, String branch, String remote, String projectsubfolder,
			CtInvocation ctAssertion) {
		JsonObject jsonsingleHelper = new JsonObject();
		jsonsingleHelper.addProperty("code", ctAssertion.toString());
		jsonsingleHelper.addProperty("line", getPosition(ctAssertion));
		CtType type = ctAssertion.getParent(CtType.class);
		jsonsingleHelper.addProperty("location", type.getQualifiedName());
		writeJsonLink(commitid, branch, remote, projectsubfolder, ctAssertion, jsonsingleHelper);
		return jsonsingleHelper;
	}

	public String cleanTypeName(String parent) {
		if (parent.length() < 6) {
			return parent;
		}
		return parent.substring(2, parent.length() - 4);
	}

	public String getRelativePath(CtElement anInvocation, ProjectRepairFacade projectFacade) {
		try {
			// System.out.println("Replacing " +
			// anInvocation.getPosition().getFile().getAbsolutePath().toLowerCase().replace("./",
			// "")
			// + "by " +
			// projectFacade.getProperties().getOriginalProjectRootDir().toLowerCase().replace("./",
			// ""));
			return anInvocation.getPosition().getFile().getAbsolutePath().replace("./", "")
					.replace(projectFacade.getProperties().getOriginalProjectRootDir().replace("./", ""), "");
		} catch (Exception e) {
			log.error("Error in position relative path");
			e.printStackTrace();
			return "NoPosition";
		}
	}

	public String createMethodSignature(CtInvocation anInvocation) {
		String signature = "";

		if (anInvocation.getExecutable() != null)

			signature +=
					//
					((anInvocation.getExecutable().getDeclaringType() != null)
							? anInvocation.getExecutable().getDeclaringType().getQualifiedName()
							: anInvocation.getExecutable().getSimpleName())
							//
							+ "#" + anInvocation.getExecutable().getSignature();

		else {
			signature += anInvocation.getShortRepresentation();
		}
		return signature;
	}

	@Override
	public Object generateOutput(RtEngine engine, String id, ProjectRepairFacade projectFacade,
			List<TestIntermediateAnalysisResult> resultByTest, List<ProgramVariant> refactors,
			Exception exceptionReceived, String out) {

		this.projectFacade = projectFacade;
		JsonObject json = null;
		if (exceptionReceived == null) {
			json = this.toJson(engine, id, this.projectFacade, resultByTest);

		} else {
			json = this.toJsonError(id, this.projectFacade, exceptionReceived);
		}

		// System.out.println("rtjsonoutput: " + json);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String ppjson = gson.toJson(json);

		String outpath = out + File.separator + "rt_" + projectFacade.getProperties().getFixid() + ".json";
		log.info("Saving json at \n" + outpath);
		try {
			FileWriter fw = new FileWriter(new File(outpath));
			fw.write(ppjson);
			fw.flush();
			fw.close();
		} catch (IOException e) {

			e.printStackTrace();
			log.error(e);
		}
		System.out.println("\nDetailed results saved on file: " + outpath);

		return json;
	}

}
