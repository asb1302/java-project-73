package hexlet.code.javaproject73.service;

import hexlet.code.javaproject73.dto.LabelDto;
import hexlet.code.javaproject73.model.Label;
import hexlet.code.javaproject73.repository.LabelRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class LabelServiceImpl implements LabelService {

    private LabelRepository labelRepository;

    @Override
    public Label createLabel(final LabelDto labelDto) {
        Label newLabel = new Label();
        newLabel.setName(labelDto.getName());
        return labelRepository.save(newLabel);
    }

    @Override
    public Label updateLabel(final LabelDto labelDto, final long id) {
        Label label = labelRepository.findById(id).get();
        label.setName(labelDto.getName());
        return labelRepository.save(label);
    }
}
