package pharmacie.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pharmacie.service.ApprovisionnementService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApprovisionnementController {

    private final ApprovisionnementService service;

    public ApprovisionnementController(ApprovisionnementService service) {
        this.service = service;
    }

    @PostMapping("/approvisionner")
    public ResponseEntity<List<String>> approvisionner() throws IOException {
        return ResponseEntity.ok(service.approvisionner());
    }

}
