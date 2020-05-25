package br.com.gabrielrps.awsimageupload.datastore;

import br.com.gabrielrps.awsimageupload.profile.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserProfileDataStore extends JpaRepository<UserProfile, UUID> {


}
