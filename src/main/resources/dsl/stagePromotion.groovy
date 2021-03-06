package dsl

def call(body = {}) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_ONLY
    body.delegate = new StagePromotionDelegate(config)
    body()

    def jenkinsUrl = "${env.JENKINS_URL}"
    def jobPath = currentBuild.getAbsoluteUrl().replace(jenkinsUrl, '/')
    def promoUrl = jobPath + "promoterebuild"
    //echo promoUrl

    manager.addBadge('clock.png', config.message, promoUrl)

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
}

