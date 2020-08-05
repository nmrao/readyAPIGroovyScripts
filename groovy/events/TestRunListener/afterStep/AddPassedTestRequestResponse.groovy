/**
* Ref: https://community.smartbear.com/t5/SoapUI-Pro/TechCorner-Challenge-9-A-script-to-include-custom-details-to-the/m-p/205582#M46988
*
*/
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep
import static com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus.*
import static com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport.HTTP_METHOD

//Closure to get ExtendedHttpMethod object
def eHM = { context.getProperty(HTTP_METHOD) }

//Initialize report 
def message = new StringBuilder()

//Add the test properties to report similar to failure case as this helps to understand easily if something is missing or incorrect
def addPropertiesToReport = { 
	message.with {
	    append('\n----------------- Properties -----------------\n')
	    append("Endpoint: ${context.endpoint}\n")
	    switch (context.currentStep) {
            case WsdlTestRequestStep:
                append("Encoding: ${context.currentStep.testRequest.encoding}\n")
                break           
            default:
            	append("HTTP Version: ${eHM().protocolVersion}\n")
            	append("Method: ${eHM().method}\n")
            	append("StatusCode: ${testStepResult.getProperty('StatusCode')}\n")
            	append("URL: ${eHM().URL}\n")
                break
       }
    }     
}

def addPayLoadToReport = { subHead, content, additionalData = null ->
	message.with {
		append("\n-------------- $subHead --------------\n")
		if(additionalData) {
			append(eHM().requestLine).append('\n')
			additionalData.each { append(it.name + ' :  ' + it.value + '\n')}
		}
		if (context.currentStep.testRequest.contentLength) { append('\n').append(content).append('\n') }
	}
}

//Actual business logic
if (([WsdlTestRequestStep, RestTestRequestStep, HttpTestRequestStep].any{context.currentStep in it}) && (testStepResult.status in [UNKNOWN, OK])) {
	addPropertiesToReport()
	addPayLoadToReport('Request', context.rawRequest, eHM().allHeaders)
	addPayLoadToReport('Response', context.rawResponse)	
    testStepResult.addMessage(message.toString())
}
