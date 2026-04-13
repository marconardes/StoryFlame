package io.storyflame.desktop;

import io.storyflame.core.publication.PublicationFormat;
import io.storyflame.core.publication.PublicationPreset;
import java.nio.file.Path;
import javax.swing.filechooser.FileNameExtensionFilter;

final class DesktopPublicationExportFormatter {
    private static final PublicationPreset[] ORDERED_PRESETS = {
            PublicationPreset.EDITORIAL_PROOF,
            PublicationPreset.DIGITAL_READING,
            PublicationPreset.QUICK_REVIEW,
            PublicationPreset.PLAIN_MANUSCRIPT
    };
    private static final PublicationFormat[] ORDERED_FORMATS = {
            PublicationFormat.PDF,
            PublicationFormat.EPUB,
            PublicationFormat.MARKDOWN,
            PublicationFormat.TXT
    };

    private DesktopPublicationExportFormatter() {
    }

    static Object[] presetOptions() {
        Object[] options = new Object[ORDERED_PRESETS.length];
        for (int index = 0; index < ORDERED_PRESETS.length; index++) {
            options[index] = presetLabel(ORDERED_PRESETS[index]);
        }
        return options;
    }

    static PublicationPreset presetFromOption(Object option) {
        if (!(option instanceof String label)) {
            return null;
        }
        for (PublicationPreset preset : ORDERED_PRESETS) {
            if (presetLabel(preset).equals(label)) {
                return preset;
            }
        }
        return null;
    }

    static String presetDialogMessage() {
        StringBuilder builder = new StringBuilder("Escolha o objetivo da publicacao:\n\n");
        for (PublicationPreset preset : ORDERED_PRESETS) {
            builder.append("- ")
                    .append(preset.label())
                    .append(": ")
                    .append(preset.description())
                    .append('\n');
        }
        return builder.toString().trim();
    }

    static String presetLabel(PublicationPreset preset) {
        return preset.label() + " -> " + buttonLabel(preset.format());
    }

    static String chooserTitle(PublicationPreset preset) {
        return "Publicar manuscrito: " + preset.label();
    }

    static Object[] formatOptions() {
        Object[] options = new Object[ORDERED_FORMATS.length];
        for (int index = 0; index < ORDERED_FORMATS.length; index++) {
            options[index] = buttonLabel(ORDERED_FORMATS[index]);
        }
        return options;
    }

    static PublicationFormat formatFromOption(Object option) {
        if (!(option instanceof String label)) {
            return null;
        }
        for (PublicationFormat format : ORDERED_FORMATS) {
            if (buttonLabel(format).equals(label)) {
                return format;
            }
        }
        return null;
    }

    static String chooserTitle(PublicationFormat format) {
        return "Publicar manuscrito em " + formatLabel(format);
    }

    static FileNameExtensionFilter chooserFilter(PublicationFormat format) {
        return new FileNameExtensionFilter(formatFilterLabel(format), format.extension().substring(1));
    }

    static String failureMessage(PublicationFormat format) {
        return "Nao foi possivel publicar o manuscrito em " + formatLabel(format) + ".";
    }

    static String cancelledStatus() {
        return DesktopOperationStatusFormatter.warning("Publicacao do manuscrito cancelada.");
    }

    static String presetChosenStatus(PublicationPreset preset) {
        return DesktopOperationStatusFormatter.success(
                "Preset selecionado: " + preset.label() + " (" + formatLabel(preset.format()) + ")."
        );
    }

    static String exportedMessage(PublicationFormat format, Path path) {
        return DesktopOperationStatusFormatter.success(
                "Manuscrito publicado em " + formatLabel(format) + " para " + path + editorialSuffix(format) + "."
        );
    }

    static String exportedAndOpenedMessage(PublicationFormat format, Path path) {
        return DesktopOperationStatusFormatter.success(
                "Manuscrito publicado em " + formatLabel(format) + " para " + path
                        + editorialSuffix(format) + ". Arquivo aberto externamente."
        );
    }

    static String exportedOpenFailedMessage(PublicationFormat format, Path path) {
        return DesktopOperationStatusFormatter.warning(
                "Manuscrito publicado em " + formatLabel(format) + " para " + path
                        + editorialSuffix(format) + ". Nao foi possivel abrir o arquivo."
        );
    }

    static String buttonLabel(PublicationFormat format) {
        return switch (format) {
            case TXT -> "Texto (.txt)";
            case MARKDOWN -> "Markdown (.md)";
            case PDF -> "PDF (.pdf)";
            case EPUB -> "EPUB (.epub)";
        };
    }

    private static String formatLabel(PublicationFormat format) {
        return switch (format) {
            case TXT -> "TXT";
            case MARKDOWN -> "Markdown";
            case PDF -> "PDF";
            case EPUB -> "EPUB";
        };
    }

    private static String formatFilterLabel(PublicationFormat format) {
        return switch (format) {
            case TXT -> "Arquivos TXT (*.txt)";
            case MARKDOWN -> "Arquivos Markdown (*.md)";
            case PDF -> "Arquivos PDF (*.pdf)";
            case EPUB -> "Arquivos EPUB (*.epub)";
        };
    }

    private static String editorialSuffix(PublicationFormat format) {
        return switch (format) {
            case PDF, EPUB -> " com formatacao editorial basica";
            case TXT, MARKDOWN -> "";
        };
    }
}
