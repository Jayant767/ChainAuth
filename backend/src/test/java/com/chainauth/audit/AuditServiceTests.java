package com.chainauth.audit;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditServiceTests {
    @Test void appendsHashLinkedEventsAndDetectsTampering(){
        AuditEventRepository repo=mock(AuditEventRepository.class);List<AuditEvent> store=new ArrayList<>();
        when(repo.findTopByOrderBySequenceDesc()).thenAnswer(i->store.stream().max(Comparator.comparingLong(AuditEvent::getSequence)));
        when(repo.save(any())).thenAnswer(i->{AuditEvent e=i.getArgument(0);store.add(e);return e;});
        when(repo.findAll(any(Pageable.class))).thenAnswer(i->{Pageable pg=i.getArgument(0);List<AuditEvent> ordered=new ArrayList<>(store);ordered.sort(Comparator.comparingLong(AuditEvent::getSequence).reversed());return new PageImpl<>(ordered.stream().skip(pg.getOffset()).limit(pg.getPageSize()).toList(),pg,ordered.size());});
        when(repo.findBySequence(anyLong())).thenAnswer(i->store.stream().filter(e->e.getSequence()==i.getArgument(0)).findFirst());
        AuditService service=new AuditService(repo);AuditEvent first=service.record("user@example.test","POST","/api/auth/login",200,"source");
        AuditEvent second=service.record("admin@example.test","PUT","/api/admin/users/1/roles",200,"source");
        assertEquals(1,first.getSequence());assertEquals(first.getEventHash(),second.getPreviousHash());assertTrue(service.verify(100));
        second.setResource("/api/admin/users/2/roles");assertFalse(service.verify(100));
    }
}
