package dsl

def call(body = {}) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_ONLY
    body.delegate = new StagePromotionDelegate(config)
    body()

    def jenkinsUrl = "${env.JENKINS_URL}"
    def jobPath = currentBuild.getAbsoluteUrl().replace(jenkinsUrl, '/')
    def majorPromoUrl = jobPath + "promoterebuild?major"
    def minorPromoUrl = jobPath + "promoterebuild?minor"
    //echo promoUrl

    if (config.size() == 1) {
        manager.addBadge('clock.png', config.message, majorPromoUrl)
    } else {
        manager.addBadge('clock.png', config.majorReleaseMessage, majorPromoUrl)
        manager.addBadge('warning.png', config.minorReleaseMessage, minorPromoUrl)
    }

}

class StagePromotionDelegate implements Serializable {
    def map

    StagePromotionDelegate(map) {
        this.map = map
        this.map['message'] = 'Promote to RELEASE'
    }

    def message(msg) {
        this.map['message'] = msg
    }

    def majorReleaseMessage(msg) {
        this.map['majorReleaseMessage'] = msg
    }

    def minorReleaseMessage(msg) {
        this.map['minorReleaseMessage'] = msg
    }
}

