package guepardoapps.lucahome.common.services.user

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

import guepardoapps.lucahome.common.adapter.DownloadAdapter
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.constants.Labels
import guepardoapps.lucahome.common.databases.user.DbUser
import guepardoapps.lucahome.common.enums.common.DownloadState
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.enums.user.UserRole
import guepardoapps.lucahome.common.models.user.User
import io.reactivex.schedulers.Schedulers

import org.junit.Assert.assertEquals
import org.junit.Before
import org.mockito.Mockito.*
import java.util.*

// Helpful link
// http://www.vogella.com/tutorials/Mockito/article.html
// https://antonioleiva.com/mockito-2-kotlin/

@RunWith(MockitoJUnitRunner::class)
class UserServiceUnitTest {

    //Class to be tested
    private lateinit var sut: UserService

    //Dependencies (will be mocked)
    private lateinit var downloadAdapterMock: DownloadAdapter
    private lateinit var dbUserMock: DbUser

    @Before
    fun setup() {
        downloadAdapterMock = mock(DownloadAdapter::class.java)
        dbUserMock = mock(DbUser::class.java)

        sut = UserService.instance
    }

    @Test
    @Throws(Exception::class)
    fun initialize_shouldSet_initialized() {
        // Assert
        assertEquals(sut.initialized, false)

        // Act
        sut.initialize(downloadAdapterMock, dbUserMock)

        // Assert
        assertEquals(sut.initialized, true)
    }

    @Test
    @Throws(Exception::class)
    fun dispose_shouldSet_initialized() {
        // Arrange
        sut.initialize(downloadAdapterMock, dbUserMock)

        // Assert
        assertEquals(sut.initialized, true)

        // Act
        sut.dispose()

        // Assert
        assertEquals(sut.initialized, false)
    }

    @Test
    @Throws(Exception::class)
    fun validate_shouldReturn_ifNotInitialized() {
        // Arrange
        val userMock = User()
        userMock.uuid = UUID.randomUUID()
        userMock.name = "UserName"
        userMock.password = "Password"
        userMock.role = UserRole.Administrator

        // Act
        sut.validate(userMock)

        // Assert
        verify(downloadAdapterMock, times(0))
                .send(
                        ServerAction.UserValidate.command,
                        ServerAction.UserValidate,
                        object : OnDownloadAdapter {
                            override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {}
                        })
        sut.responsePublishSubject
                .subscribeOn(Schedulers.io())
                .subscribe { result ->
                    assertEquals(false, result.success)
                    assertEquals(Labels.Services.notInitialized, result.message)
                    assertEquals(ServerAction.UserValidate, result.action)
                }
    }
}