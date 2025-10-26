package gemshop.web;

import gemshop.domain.Gemstone;
import gemshop.service.GemstoneService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/gemstones")
public class GemstoneController {

    private final GemstoneService service;

    public GemstoneController(GemstoneService service) {
        this.service = service;
    }

    @GetMapping
    public List<Gemstone> listAll() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public Gemstone getOne(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/ping")
    public String ping() { return "pong"; }
}
