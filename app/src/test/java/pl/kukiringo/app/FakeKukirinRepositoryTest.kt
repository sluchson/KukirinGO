package pl.kukiringo.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.kukiringo.app.data.repository.FakeKukirinRepository

class FakeKukirinRepositoryTest {

    @Test
    fun joiningMeetupAddsParticipant() {
        val repo = FakeKukirinRepository()
        val before = repo.meetups.value.first { it.id == "m1" }

        repo.toggleJoinMeetup("m1")

        val after = repo.meetups.value.first { it.id == "m1" }
        assertTrue(after.joined)
        assertEquals(before.participants + 1, after.participants)
    }

    @Test
    fun likingClipTwiceRestoresCount() {
        val repo = FakeKukirinRepository()
        val before = repo.clips.value.first { it.id == "c1" }.likes

        repo.toggleLikeClip("c1")
        repo.toggleLikeClip("c1")

        assertEquals(before, repo.clips.value.first { it.id == "c1" }.likes)
    }
}
