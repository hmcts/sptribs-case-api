package uk.gov.hmcts.sptribs.controllers.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.sptribs.notification.model.Correspondence;

@Controller
@RequestMapping(path="/st-update-db")
@Slf4j
public class MainController {

    @Autowired
    private CorrespondenceRepository correspondenceRepository;

    @PostMapping(path="/add-correspondence")
    public void addNewCorrespondence(@RequestParam Correspondence correspondence) {
        correspondenceRepository.save(correspondence);
        log.debug("Correspondence saved");
    }

}
