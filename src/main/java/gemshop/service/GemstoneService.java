package gemshop.service;

import gemshop.domain.Gemstone;
import gemshop.repo.GemstoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GemstoneService {

    private final GemstoneRepository repo;

    public GemstoneService(GemstoneRepository repo) {
        this.repo = repo;
    }

    public List<Gemstone> listAll() {
        return repo.findAll();
    }

    public Gemstone getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Gemstone not found"));
    }

    public Gemstone create(Gemstone g) {
        return repo.save(g);
    }

    public Gemstone update(Long id, Gemstone g) {
        Gemstone cur = getById(id);
        // simple dumb copy
        cur.setName(g.getName());
        cur.setStoneType(g.getStoneType());
        cur.setCarat(g.getCarat());
        cur.setQuality(g.getQuality());
        cur.setBasePrice(g.getBasePrice());
        return repo.save(cur);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
