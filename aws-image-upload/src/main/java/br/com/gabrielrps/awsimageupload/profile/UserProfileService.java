package br.com.gabrielrps.awsimageupload.profile;

import br.com.gabrielrps.awsimageupload.buckets.BucketName;
import br.com.gabrielrps.awsimageupload.datastore.UserProfileDataStore;
import br.com.gabrielrps.awsimageupload.filestore.FileStore;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class UserProfileService {

    private final UserProfileDataStore userProfileDataStore;
    private final FileStore fileStore;

    @Autowired
    public UserProfileService(UserProfileDataStore userProfileDataStore, FileStore fileStore) {
        this.userProfileDataStore = userProfileDataStore;
        this.fileStore = fileStore;
    }

    public List<UserProfile> getUserProfiles(){
        return userProfileDataStore.findAll();
    }

    public ResponseEntity<?> uploadUserProfileImage(UUID userProfileId, MultipartFile file) {
        if (file.isEmpty()){
            throw new IllegalStateException("Cannot upload empty file");
        }
        if(!Arrays.asList(ContentType.IMAGE_JPEG.getMimeType(), ContentType.IMAGE_PNG.getMimeType(), ContentType.IMAGE_GIF.getMimeType()).contains(file.getContentType())){
            return new ResponseEntity<>("File must be an image", HttpStatus.BAD_REQUEST);
        }

        UserProfile user = getUserProfileOrThrow(userProfileId);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type",file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));

        String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), user.getUserProfileId());
        String filename = String.format("%s-%s", file.getOriginalFilename(), UUID.randomUUID());

        try {
            fileStore.save(path, filename, Optional.of(metadata), file.getInputStream());
            user.setUserProfileImageLink(filename);

            userProfileDataStore.save(user);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private UserProfile getUserProfileOrThrow(UUID userProfileId) {
        return getUserProfiles()
                .stream()
                .filter(userProfile -> userProfile.getUserProfileId().equals(userProfileId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("User profile not found"));
    }

    public byte[] downloadUserProfileImage(UUID userProfileId) {
        UserProfile user = getUserProfileOrThrow(userProfileId);
        String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), user.getUserProfileId());

        return user.getUserProfileImageLink()
                .map(link -> fileStore.download(path, link))
                .orElse(new byte[0]);

    }
}
