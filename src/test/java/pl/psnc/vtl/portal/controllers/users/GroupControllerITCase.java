package pl.psnc.vtl.portal.controllers.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.dto.users.GroupCore;
import pl.psnc.vtl.portal.dto.users.UserGroupCore;
import pl.psnc.vtl.portal.dto.users.UserId;
import pl.psnc.vtl.portal.dto.users.UserRoleId;
import pl.psnc.vtl.portal.model.users.User;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GroupControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String TOKEN;

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";

    ObjectMapper objectMapper = new ObjectMapper();


    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
    }


    @Test
    public void shouldReturnGroup()
            throws Exception {
        Integer groupId = 4;

        mvc.perform(get("/api/groups/{groupId}", groupId).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN)).andExpect(status().isOk()).andExpect(jsonPath("id", is(groupId)))
                .andExpect(jsonPath("name", is("groupToRename"))).andExpect(jsonPath("owner", is(true)));
    }


    @Test
    public void shouldNotReturnGroupWhenNotExist()
            throws Exception {
        Integer nonExistingGroupId = 999;

        mvc.perform(get("/api/groups/{groupId}", nonExistingGroupId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }


    @Test
    public void shouldNotReturnGroupWhenHasNotAdminRole()
            throws Exception {
        Long groupId = 3L;

        mvc.perform(get("/api/groups/{groupId}", groupId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }


    @Test
    public void shouldDeleteGroupThatAffectsAndNotAffectPermissions()
            throws Exception {
        Integer groupId = 2;

        String email3Token = LogInService.getJWTToken(mvc, "email3@wp.pl", "myPassword");

        mvc.perform(get("/api/transcriptions/{transcriptionId}", 10).header("Authorization", email3Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("id", is(10)))
                .andExpect(jsonPath("viewMode", is("admin")));

        mvc.perform(get("/api/transcriptions/{transcriptionId}", 11).header("Authorization", email3Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("id", is(11)))
                .andExpect(jsonPath("viewMode", is("admin")));

        mvc.perform(delete("/api/groups/{groupId}", groupId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        mvc.perform(get("/api/transcriptions/{transcriptionId}", 10).header("Authorization", email3Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("id", is(10)))
                .andExpect(jsonPath("viewMode", is("admin")));
        
        mvc.perform(get("/api/transcriptions/{transcriptionId}", 11).header("Authorization", email3Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("id", is(11)))
                .andExpect(jsonPath("viewMode", is("read-only")));
    }


    @Test
    public void shouldNotDeleteGroupWhenHasNotOwnerRole()
            throws Exception {
        Long groupId = 3L;

        mvc.perform(delete("/api/groups/{groupId}", groupId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }


    @Test
    public void shouldNotDeleteGroupWhenNotExists()
            throws Exception {
        Integer nonExistingGroupId = 999;

        mvc.perform(delete("/api/groups/{groupId}", nonExistingGroupId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }


    @Test
    public void shouldRemoveUserFromGroupThatAffectsPermissions()
            throws Exception {
        Long groupId = 4L;
        UserId userId = new UserId();
        userId.setId(4L);

        String email3Token = LogInService.getJWTToken(mvc, "email3@wp.pl", "myPassword");

        mvc.perform(get("/api/transcriptions/{transcriptionId}", 9).header("Authorization", email3Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("id", is(9)))
                .andExpect(jsonPath("viewMode", is("admin")));

        mvc.perform(delete("/api/users-groups/{groupId}", groupId).header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(userId)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/api/transcriptions/{transcriptionId}", 9).header("Authorization", email3Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("id", is(9)))
                .andExpect(jsonPath("viewMode", is("read-only")));
    }


    @Test
    public void shouldRemoveUserFromGroupThatNotAffectPermissions()
            throws Exception {
        Long groupId = 4L;
        UserId userId = new UserId();
        userId.setId(5L);

        String email4Token = LogInService.getJWTToken(mvc, "email4@wp.pl", "myPassword");

        mvc.perform(get("/api/transcriptions/{transcriptionId}", 10).header("Authorization", email4Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("id", is(10)))
                .andExpect(jsonPath("viewMode", is("verify")));

        mvc.perform(delete("/api/users-groups/{groupId}", groupId).header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(userId)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/api/transcriptions/{transcriptionId}", 10).header("Authorization", email4Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("id", is(10)))
                .andExpect(jsonPath("viewMode", is("verify")));
    }


    @Test
    public void shouldNotRemoveUserFromGroupWhenHasNotAdminRole()
            throws Exception {
        Long groupId = 3L;
        UserId userId = new UserId();
        userId.setId(4L);



        mvc.perform(delete("/api/users-groups/{groupId}", groupId).header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(userId)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }


    @Test
    public void shouldNotRemoveUserFromGroupWhenUserIsCreator()
            throws Exception {
        int groupId = 4;

        UserId userId = new UserId();
        userId.setId(3L);


        mvc.perform(delete("/api/users-groups/{groupId}", groupId).header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(userId)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void shouldNotRemoveUserFromGroupWhenGroupDoesNotExists()
            throws Exception {
        int groupId = 999;
        UserId userId = new UserId();
        userId.setId(2L);

        mvc.perform(delete("/api/users-groups/{groupId}", groupId).header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(userId)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    public void shouldAddGroup()
            throws Exception {
        String groupName = "newGroup";

        GroupCore newGroup = new GroupCore();
        newGroup.setName(groupName);

        mvc.perform(post("/api/groups", newGroup).header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(newGroup)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("name", is(groupName)));
    }


    @Test
    public void shouldAddUserToGroupThatAffectsPermissions()
            throws Exception {
        Long groupId = 1L;
        String email = "email4@wp.pl";
        String emailJson = "{ \"email\": \""+email +"\"}";

        String email4Token = LogInService.getJWTToken(mvc, "email4@wp.pl", "myPassword");

        mvc.perform(get("/api/transcriptions/{transcriptionId}", 1).header("Authorization", email4Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("id", is(1)))
                .andExpect(jsonPath("viewMode", is("read-only")));

        User user = new User();
        user.setEmail(email);

        mvc.perform(post("/api/users-groups/{groupId}", groupId).header("Authorization", TOKEN)
                .content(emailJson).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("id", is(1)))
                .andExpect(jsonPath("members[3].user.email", is(email))).andExpect(jsonPath("owner", is(false)));

        mvc.perform(get("/api/transcriptions/{transcriptionId}", 1).header("Authorization", email4Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("id", is(1)))
                .andExpect(jsonPath("viewMode", is("transcribe")));
    }


    @Test
    public void shouldNotAddUserToGroupWhenHasNotAdminRole()
            throws Exception {
        Long groupId = 3L;
        String email = "{\"email\":\"email3@wp.pl\"}";

        mvc.perform(post("/api/users-groups/{groupId}", groupId).header("Authorization", TOKEN)
                .content(email).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }


    @Test
    public void shouldNotAddUserToGroupWhenUserAlreadyExists()
            throws Exception {
        Long groupId = 1L;
        String email = "{\"email\":\"email5@wp.pl\"}";


        mvc.perform(post("/api/users-groups/{groupId}", groupId).header("Authorization", TOKEN)
                .content(email).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code", is("groups.error.userAlreadyExistsInGroup")));
    }


    @Test
    public void shouldUpdateUserRoleInGroup()
            throws Exception {
        Long groupId = 1L;
        long userId = 4L;
        String email = "email3@wp.pl";
        String roleCode = "contributor";
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        UserRoleId role = new UserRoleId();
        role.setCode(roleCode);
        UserGroupCore userGroup = new UserGroupCore();
        userGroup.setUser(user);
        userGroup.setRole(role);

        mvc.perform(put("/api/users-groups/{groupId}", groupId).header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(userGroup)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("members[2].user.email", is(email)))
                .andExpect(jsonPath("members[2].role.code", is(roleCode)));
    }


    @Test
    public void shouldUpdateGroup()
            throws Exception {
        Long groupId = 4L;

        GroupCore updatedUserGroup = new GroupCore();
        String name = "newUpdatedGroupName";
        updatedUserGroup.setName(name);

        mvc.perform(put("/api/groups/{groupId}", groupId).header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(updatedUserGroup)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("name", is(name)));
    }


    @Test
    public void shouldNotUpdateGroupWhenGroupDoesNotExists()
            throws Exception {
        Long groupId = 999L;

        GroupCore updatedUserGroup = new GroupCore();
        String name = "newUpdatedGroupName";
        updatedUserGroup.setName(name);

        mvc.perform(put("/api/groups/{groupId}", groupId).header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(updatedUserGroup)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }


    @Test
    public void shouldNotUpdateGroupWhenHasNotAdminAccess()
            throws Exception {
        Long groupId = 1L;

        GroupCore updatedUserGroup = new GroupCore();
        String name = "newUpdatedGroupName";
        updatedUserGroup.setName(name);

        mvc.perform(put("/api/groups/{groupId}", groupId).header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(updatedUserGroup)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }


    @Test
    public void shouldGetGroupsByNameWhenNoListCountGiven()
            throws Exception {
        String pattern = "group";

        mvc.perform(get("/api/groups/my-groups/{pattern}", pattern).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }


    @Test
    public void shouldGetGroupsByNameWhenNoListCountGivenAndPatternDoNotMatchesAll()
            throws Exception {
        String pattern = "to";

        mvc.perform(get("/api/groups/my-groups/{pattern}", pattern).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }


    @Test
    public void shouldGetGroupsByNameWhenListCountIsGiven()
            throws Exception {
        String pattern = "group";

        mvc.perform(get("/api/groups/my-groups/{pattern}", pattern).header("Authorization", TOKEN)
                .param("listCount", "1").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

}
