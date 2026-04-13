package io.storyflame.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DesktopDraftStateFormatterTest {
    @Test
    void formatsCharacterDraftStates() {
        assertEquals(
                "Preencha nome e descricao. Depois clique em Salvar personagem.",
                DesktopDraftStateFormatter.characterDraftHint(false, "", "")
        );
        assertEquals(
                "Preencha o nome do personagem. Depois clique em Salvar personagem.",
                DesktopDraftStateFormatter.characterDraftHint(true, "", "Descricao")
        );
        assertEquals(
                "Preencha a descricao. Depois clique em Salvar personagem.",
                DesktopDraftStateFormatter.characterDraftHint(true, "Lia", "")
        );
        assertEquals(
                "Pronto para salvar. Clique em Salvar personagem.",
                DesktopDraftStateFormatter.characterDraftHint(true, "Lia", "Pilota")
        );
    }

    @Test
    void formatsTagDraftStates() {
        assertEquals(
                "Preencha id, rotulo e texto renderizado. Depois clique em Salvar tag.",
                DesktopDraftStateFormatter.tagDraftHint(false, "", "", "")
        );
        assertEquals(
                "Preencha o id da tag no formato 4 letras + numero.",
                DesktopDraftStateFormatter.tagDraftHint(true, "", "Beat", "texto")
        );
        assertEquals(
                "Use id no formato 4 letras + numero, como falc1.",
                DesktopDraftStateFormatter.tagDraftHint(true, "beat", "Beat", "texto")
        );
        assertEquals(
                "Preencha o rotulo da tag. Depois clique em Salvar tag.",
                DesktopDraftStateFormatter.tagDraftHint(true, "beat1", "", "texto")
        );
        assertEquals(
                "Preencha o texto renderizado. Depois clique em Salvar tag.",
                DesktopDraftStateFormatter.tagDraftHint(true, "beat1", "Beat", "")
        );
        assertEquals(
                "Pronto para salvar. Clique em Salvar tag.",
                DesktopDraftStateFormatter.tagDraftHint(true, "beat1", "Beat", "texto")
        );
    }

    @Test
    void formatsTagStatusStates() {
        assertEquals("Rascunho de tag", DesktopDraftStateFormatter.tagStatus(false, "", "", false));
        assertEquals("Falta rotulo", DesktopDraftStateFormatter.tagStatus(true, "", "texto", false));
        assertEquals("Tag de personagem", DesktopDraftStateFormatter.tagStatus(true, "Lia", "texto", true));
        assertEquals("Falta texto renderizado", DesktopDraftStateFormatter.tagStatus(true, "Beat", "", false));
        assertEquals("Texto renderizado pronto", DesktopDraftStateFormatter.tagStatus(true, "Beat", "texto", false));
    }
}
