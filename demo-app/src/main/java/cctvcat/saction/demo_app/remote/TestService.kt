package cctvcat.saction.demo_app.remote

import cctvcat.saction.demo_app.ITestService
import rikka.hidden.compat.UserManagerApis

class TestService : ITestService.Stub() {

    override fun getUserIds(): MutableList<Int> {
        return UserManagerApis
            .getUserIdsNoThrow(false, false, false)
            .toMutableList()
    }

}