package pl.psnc.vtl.portal.controllers.transcriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.LogInService;

import org.springframework.http.MediaType;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionUserCore;
import pl.psnc.vtl.portal.dto.users.GroupIdCore;
import pl.psnc.vtl.portal.dto.users.UserId;



import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@Transactional
public class TranscriptionPrivilegesControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String TOKEN;

    ObjectMapper objectMapper = new ObjectMapper();

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";


    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
    }


    @Test
    public void shouldAddUserToTranscriptionWhenHasPermission()
            throws Exception {
        long transcriptionId = 1L;
        String email = "{ \"email\": \"email4@wp.pl\"}";


        mvc.perform(post("/api/transcriptions/{transcriptionId}/allowed-users", transcriptionId)
                .header("Authorization", TOKEN).content(email)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }


    @Test
    public void shouldNotAddUserToTranscriptionWhenHasNoPermission()
            throws Exception {
        long transcriptionId = 1L;
        String email = "{ \"email\": \"email4@wp.pl\"}";

        String token = LogInService.getJWTToken(mvc, "email3@wp.pl", PASSWORD);

        mvc.perform(post("/api/transcriptions/{transcriptionId}/allowed-users", transcriptionId)
                .header("Authorization", token).content(email)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }


    @Test
    public void shouldNotAddUserToTranscriptionWhenAlreadyExists()
            throws Exception {
        long transcriptionId = 1L;
        String email = "{ \"email\": \"email2@wp.pl\"}";

        mvc.perform(post("/api/transcriptions/{transcriptionId}/allowed-users", transcriptionId)
                .header("Authorization", TOKEN).content(email)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("code", is("transcriptions.error.transcriptionUserAlreadyExists")));
    }


    @Test
    public void shouldNotAddUserToTranscriptionWhenThereIsNoGivenTranscription()
            throws Exception {
        long nonExistingTranscriptionId = 999L;
        String email = "{ \"email\": \"email2@wp.pl\"}";

        mvc.perform(post("/api/transcriptions/{transcriptionId}/allowed-users", nonExistingTranscriptionId)
                .header("Authorization", TOKEN).content(email)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());

    }


    @Test
    public void shouldNotAddUserToTranscriptionWhenThereIsNoGivenUser()
            throws Exception {
        long nonExistingTranscriptionId = 1L;
        String email = "{ \"email\": \"no-existing-email@wp.pl\"}";


        mvc.perform(post("/api/transcriptions/{transcriptionId}/allowed-users", nonExistingTranscriptionId)
                .header("Authorization", TOKEN).content(email)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("code", is("transcriptions.error.noUserWithGivenEmail")));;
    }

    @Test
    public void shouldUpdateUserRoleInTranscriptionWhenHasPermission()
            throws Exception {
        long transcriptionId = 1L;
        long userId = 13L;
        String roleCode = "verifier";

        TranscriptionUserCore transcriptionUserCore = new TranscriptionUserCore();
        transcriptionUserCore.setUserId(userId);
        transcriptionUserCore.setRoleCode(roleCode);

        mvc.perform(put("/api/transcriptions/{transcriptionId}/allowed-users", transcriptionId)
                .header("Authorization", TOKEN).content(objectMapper.writeValueAsString(transcriptionUserCore))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("[3]user.id", is(13)))
                .andExpect(jsonPath("[3].role.code", is(roleCode)));
    }


    @Test
    public void shouldNotUpdateUserRoleInTranscriptionWhenHasNoPermission()
            throws Exception {
        long transcriptionId = 1L;
        long userId = 5L;
        String roleCode = "contributor";

        TranscriptionUserCore transcriptionUserCore = new TranscriptionUserCore();
        transcriptionUserCore.setUserId(userId);
        transcriptionUserCore.setRoleCode(roleCode);

        String token = LogInService.getJWTToken(mvc, "email3@wp.pl", PASSWORD);

        mvc.perform(put("/api/transcriptions/{transcriptionId}/allowed-users", transcriptionId)
                .header("Authorization", token).content(objectMapper.writeValueAsString(transcriptionUserCore))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }


    @Test
    public void shouldNotUpdateUserRoleInTranscriptionWhenThereIsNoGivenTranscription()
            throws Exception {
        long nonExistingTranscriptionId = 999L;
        long userId = 1L;
        String roleCode = "contributor";

        TranscriptionUserCore transcriptionUserCore = new TranscriptionUserCore();
        transcriptionUserCore.setUserId(userId);
        transcriptionUserCore.setRoleCode(roleCode);

        mvc.perform(put("/api/transcriptions/{transcriptionId}/allowed-users", nonExistingTranscriptionId)
                .header("Authorization", TOKEN).content(objectMapper.writeValueAsString(transcriptionUserCore))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());

    }


    @Test
    public void shouldNotUpdateUserRoleInTranscriptionWhenThereIsNoGivenUser()
            throws Exception {
        long nonExistingTranscriptionId = 1L;
        long userId = -1L;
        String roleCode = "contributor";

        TranscriptionUserCore transcriptionUserCore = new TranscriptionUserCore();
        transcriptionUserCore.setUserId(userId);
        transcriptionUserCore.setRoleCode(roleCode);

        mvc.perform(put("/api/transcriptions/{transcriptionId}/allowed-users", nonExistingTranscriptionId)
                .header("Authorization", TOKEN).content(objectMapper.writeValueAsString(transcriptionUserCore))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound()) ;
    }


    @Test
    public void shouldDeleteUserFromTranscriptionWhenHasPermission()
            throws Exception {
        UserId userId = new UserId();
        userId.setId(6L);
        long transcriptionId = 1L;

        mvc.perform(delete("/api/transcriptions/{transcriptionId}/allowed-users", transcriptionId)
                .header("Authorization", TOKEN).content(objectMapper.writeValueAsString(userId))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

    }


    @Test
    public void shouldNotDeleteUserFromTranscriptionWhenHasNoPermission()
            throws Exception {
        long transcriptionId = 1L;
        UserId userId = new UserId();
        userId.setId(6L);

        String token = LogInService.getJWTToken(mvc, "email3@wp.pl", PASSWORD);

        mvc.perform(delete("/api/transcriptions/{transcriptionId}/allowed-users", transcriptionId)
                .header("Authorization", token).content(objectMapper.writeValueAsString(userId))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }


    @Test
    public void shouldNotDeleteUserFromTranscriptionWhenDoesntExistsInExists()
            throws Exception {
        long transcriptionId = 1L;
        UserId userId = new UserId();
        userId.setId(4L);


        mvc.perform(delete("/api/transcriptions/{transcriptionId}/allowed-users", transcriptionId)
                .header("Authorization", TOKEN).content(objectMapper.writeValueAsString(userId))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }


    @Test
    public void shouldNotDeleteUserFromTranscriptionWhenUserIsCreator()
            throws Exception {
        long transcriptionId = 1L;
        UserId userId = new UserId();
        userId.setId(1L);


        mvc.perform(delete("/api/transcriptions/{transcriptionId}/allowed-users", transcriptionId)
                .header("Authorization", TOKEN).content(objectMapper.writeValueAsString(userId))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andExpect(
                jsonPath("code", is("transcriptions.error.cannotDeleteTranscriptionCreatorFromTranscription")));

    }


    @Test
    public void shouldNotDeleteUserFromTranscriptionWhenThereIsNoGivenTranscription()
            throws Exception {
        long nonExistingTranscriptionId = 999L;
        UserId userId = new UserId();
        userId.setId(6L);



        mvc.perform(delete("/api/transcriptions/{transcriptionId}/allowed-users", nonExistingTranscriptionId)
                .header("Authorization", TOKEN).content(objectMapper.writeValueAsString(userId))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }


    @Test
    public void shouldNotDeleteUserFromTranscriptionWhenThereIsNoGivenUser()
            throws Exception {
        long nonExistingTranscriptionId = 1L;
        UserId userId = new UserId();
        userId.setId(-1L);


        mvc.perform(delete("/api/transcriptions/{transcriptionId}/allowed-users", nonExistingTranscriptionId)
                .header("Authorization", TOKEN).content(objectMapper.writeValueAsString(userId))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound()) ;
    }

    @Test
    public void shouldAddGroupToTranscriptionWhenHasPermission()
            throws Exception {
        long transcriptionId = 1L;
        long groupId = 3L;

        GroupIdCore groupIdCore = new GroupIdCore();
        groupIdCore.setGroupId(groupId);

        mvc.perform(post("/api/transcriptions/{transcriptionId}/allowed-groups", transcriptionId)
                .header("Authorization", TOKEN).content(objectMapper.writeValueAsString(groupIdCore))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }


    @Test
    public void shouldNotAddGroupToTranscriptionWhenHasNoPermission()
            throws Exception {
        long transcriptionId = 1L;
        long groupId = 3L;

        GroupIdCore groupIdCore = new GroupIdCore();
        groupIdCore.setGroupId(groupId);

        String token = LogInService.getJWTToken(mvc, "email3@wp.pl", PASSWORD);

        mvc.perform(post("/api/transcriptions/{transcriptionId}/allowed-groups", transcriptionId)
                .header("Authorization", token).content(objectMapper.writeValueAsString(groupIdCore))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }


    @Test
    public void shouldNotAddGroupToTranscriptionWhenAlreadyExists()
            throws Exception {
        long transcriptionId = 1L;
        long groupId = 1L;

        GroupIdCore groupIdCore = new GroupIdCore();
        groupIdCore.setGroupId(groupId);

        mvc.perform(post("/api/transcriptions/{transcriptionId}/allowed-groups", transcriptionId)
                .header("Authorization", TOKEN).content(objectMapper.writeValueAsString(groupIdCore))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("code", is("transcriptions.error.transcriptionGroupAlreadyExists")));
    }


    @Test
    public void shouldNotAddGroupToTranscriptionWhenThereIsNoGivenTranscription()
            throws Exception {
        long nonExistingTranscriptionId = 999L;
        long groupId = 3L;

        GroupIdCore groupIdCore = new GroupIdCore();
        groupIdCore.setGroupId(groupId);

        mvc.perform(post("/api/transcriptions/{transcriptionId}/allowed-groups", nonExistingTranscriptionId)
                .header("Authorization", TOKEN).content(objectMapper.writeValueAsString(groupIdCore))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());

    }


    @Test
    public void shouldNotAddGroupToTranscriptionWhenThereIsNoGivenGroup()
            throws Exception {
        long nonExistingTranscriptionId = 1L;
        long groupId = 999L;

        GroupIdCore groupIdCore = new GroupIdCore();
        groupIdCore.setGroupId(groupId);

        mvc.perform(post("/api/transcriptions/{transcriptionId}/allowed-groups", nonExistingTranscriptionId)
                .header("Authorization", TOKEN).content(objectMapper.writeValueAsString(groupIdCore))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
                .andExpect(jsonPath("error", is("transcriptions.error.groupNotFound")));
    }


    @Test
    public void shouldDeleteGroupFromTranscriptionWhenHasPermission()
            throws Exception {
        long transcriptionId = 1L;
        long groupId = 1L;

        mvc.perform(delete("/api/transcriptions/{transcriptionId}/allowed-groups/{groupId}", transcriptionId, groupId)
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

    }


    @Test
    public void shouldNotDeleteGroupFromTranscriptionWhenHasNoPermission()
            throws Exception {
        long transcriptionId = 1L;
        long groupId = 3L;

        String token = LogInService.getJWTToken(mvc, "email3@wp.pl", PASSWORD);

        mvc.perform(delete("/api/transcriptions/{transcriptionId}/allowed-groups/{groupId}", transcriptionId, groupId)
                .header("Authorization", token).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }


    @Test
    public void shouldNotDeleteGroupFromTranscriptionWhenDoesntExists()
            throws Exception {
        long transcriptionId = 1L;
        long groupId = 3L;

        mvc.perform(delete("/api/transcriptions/{transcriptionId}/allowed-groups/{groupId}", transcriptionId, groupId)
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    public void shouldNotDeleteGroupFromTranscriptionWhenThereIsNoGivenTranscription()
            throws Exception {
        long nonExistingTranscriptionId = 999L;
        long groupId = 3L;

        mvc.perform(
                delete("/api/transcriptions/{transcriptionId}/allowed-groups/{groupId}", nonExistingTranscriptionId,
                        groupId).header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    public void shouldNotDeleteGroupFromTranscriptionWhenThereIsNoGivenGroup()
            throws Exception {
        long nonExistingTranscriptionId = 1L;
        long groupId = 999L;

        mvc.perform(
                delete("/api/transcriptions/{transcriptionId}/allowed-groups/{groupId}", nonExistingTranscriptionId,
                        groupId).header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("error", is("transcriptions.error.groupNotFound")));
    }

}
