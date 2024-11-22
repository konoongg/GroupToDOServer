package ru.nsu.db.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nsu.db.repositoris.InvitationRepository;
import ru.nsu.db.tables.Invitation;

import java.util.List;

@Service
public class InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;

    public Invitation createInvitation(Invitation invitation) {
        return invitationRepository.save(invitation);
    }


    public Invitation findById(Long invitationId) {
        return invitationRepository.findById(invitationId).orElse(null);
    }

    public Invitation updateInvitation(Invitation invitation) {
        return invitationRepository.save(invitation);
    }

    public List<Invitation> findByUserFrom(Long userId) {
        return invitationRepository.findByUserFromId(userId);
    }

    public List<Invitation> findByUserTo(Long userId) {
        return invitationRepository.findByUserToId(userId);
    }

    public void deleteInvitation(Long id) {
        invitationRepository.deleteById(id);
    }
}