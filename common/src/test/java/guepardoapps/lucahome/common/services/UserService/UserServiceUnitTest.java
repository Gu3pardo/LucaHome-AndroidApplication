package guepardoapps.lucahome.common.services.UserService;
/*
import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import guepardoapps.lucahome.common.adapter.DownloadAdapter;
import guepardoapps.lucahome.common.databases.user.DbUser;
import guepardoapps.lucahome.common.services.user.UserService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

// Helpful link
// http://www.vogella.com/tutorials/Mockito/article.html
// https://antonioleiva.com/mockito-2-kotlin/

@RunWith(MockitoJUnitRunner.class)
public class UserServiceUnitTest {

    @Test
    public void initialize_shouldSet_initialized() throws Exception {
        DownloadAdapter downloadAdapterMock = mock(DownloadAdapter.class);
        DbUser dbUserMock = mock(DbUser.class);

        UserService sut = UserService.Companion.getInstance();

        assertEquals(sut.getInitialized(), false);

        Context context = mock(Context.class);
        sut.initialize(context);

        assertEquals(sut.getInitialized(), true);
    }

    @Test
    public void dispose_shouldSet_initialized() throws Exception {
        DownloadAdapter downloadAdapterMock = mock(DownloadAdapter.class);
        DbUser dbUserMock = mock(DbUser.class);

        UserService sut = UserService.Companion.getInstance();
        Context context = mock(Context.class);
        sut.initialize(context);

        assertEquals(sut.getInitialized(), true);

        sut.dispose();

        assertEquals(sut.getInitialized(), false);
    }
}
*/