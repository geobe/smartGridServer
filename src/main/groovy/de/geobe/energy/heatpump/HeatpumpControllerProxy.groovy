package de.geobe.energy.heatpump

import de.geobe.energy.config.Configuration

class HeatpumpControllerProxy {
    private hpcProtocol
    private hpcHost
    private hpcPort
    private hpcPath
    private URL hpcUrl

    static void main(String[] args) {
        HeatpumpControllerProxy controller = new HeatpumpControllerProxy()
        while (true) {
            print "Eingabe Normal, Suspend, Precedence, Enforced, Aktuell, eXit > "
            def r = java.lang.System.in.newReader().readLine()
            if (r.toUpperCase().startsWith('N')) {
                controller.state = HeatPumpState.NORMALOPERATION
            } else if (r.toUpperCase().startsWith('P')) {
                controller.state = HeatPumpState.PRECEDENCE
            } else if (r.toUpperCase().startsWith('S')) {
                controller.state = HeatPumpState.SUSPENDED
            } else if (r.toUpperCase().startsWith('A')) {
                println "${controller.state}"
            } else if (r.toUpperCase().startsWith('E')) {
                try {
                    controller.state = HeatPumpState.ENFORCED
                } catch (Exception e) {
                    println e
                }
            } else if (r.toUpperCase().startsWith('X')) {
                break
            } else {
                println "read: $r"
            }
        }
    }

    HeatpumpControllerProxy(String configfile = 'testConfig.cfg') {
        def config = new Configuration().init(configfile)
        hpcProtocol = config.hpcapi.protocol
        hpcHost = config.hpcapi.host
        hpcPort = config.hpcapi.port
        hpcPath = config.hpcapi.path
        hpcUrl = new URL(hpcProtocol, hpcHost, hpcPort, hpcPath)
    }

    def getState() {
        def hpcConn = hpcUrl.openConnection()
        hpcConn.setRequestProperty('Accept', 'text/json')
        def respCode = hpcConn.getResponseCode()
        def response
        if(respCode == 200) {
            response = hpcConn.inputStream.text
        } else {
            response = respCode
        }
        response
    }

    def setState(HeatPumpState state) {
        def hpcConn = hpcUrl.openConnection()
        hpcConn.setRequestProperty('Accept', 'text/json')
        hpcConn.with {
            doOutput = true
            requestMethod = 'POST'
            outputStream.with { writer ->
                writer << "sg-state=$state".toString()
            }
            println content.text
        }
    }
}
