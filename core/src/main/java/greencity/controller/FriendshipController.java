package greencity.controller;

import greencity.annotations.CurrentUser;
import greencity.constant.HttpStatuses;
import greencity.dto.friendship.CountOfFriendsDto;
import greencity.dto.user.UserVO;
import greencity.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/friendship")
public class FriendshipController {
    private final FriendshipService friendshipService;

    /**
     * Returns number of friendships for authenticated {@link UserVO}.
     *
     * @return {@link ResponseEntity} with friendships count.
     */
    @Operation(summary = "Get count of friendships for user")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = HttpStatuses.OK,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CountOfFriendsDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED,
            content = @Content(schema = @Schema))
    })
    @GetMapping("/getCountOfFriendships")
    public ResponseEntity<CountOfFriendsDto> getCountOfFriendshipsForUser(
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity.ok(
            new CountOfFriendsDto(
                friendshipService.countByUserIdAndStatus(userVO.getId())));
    }
}
