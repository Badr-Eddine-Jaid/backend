package pharmacie.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pharmacie.dao.MedicamentRepository;
import pharmacie.entity.Categorie;
import pharmacie.entity.Fournisseur;
import pharmacie.entity.Medicament;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApprovisionnementService {

    private final MedicamentRepository medicamentRepository;

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    public ApprovisionnementService(MedicamentRepository medicamentRepository) {
        this.medicamentRepository = medicamentRepository;
    }

    @Transactional
    public List<String> approvisionner() throws IOException {
        List<Medicament> aReappro = medicamentRepository.findMedicamentsAReapprovisionner();

        if (aReappro.isEmpty()) {
            return List.of("Aucun médicament à réapprovisionner.");
        }

        Map<Categorie, List<Medicament>> parCategorie = aReappro.stream()
            .collect(Collectors.groupingBy(Medicament::getCategorie));

        Map<Fournisseur, Map<Categorie, List<Medicament>>> mailParFournisseur = new HashMap<>();
        for (Map.Entry<Categorie, List<Medicament>> entry : parCategorie.entrySet()) {
            for (Fournisseur f : entry.getKey().getFournisseurs()) {
                mailParFournisseur
                    .computeIfAbsent(f, k -> new HashMap<>())
                    .put(entry.getKey(), entry.getValue());
            }
        }

        List<String> resultats = new ArrayList<>();
        for (Map.Entry<Fournisseur, Map<Categorie, List<Medicament>>> entry : mailParFournisseur.entrySet()) {
            Fournisseur fournisseur = entry.getKey();
            envoyerMail(fournisseur.getEmail(), fournisseur.getNom(), construireCorpsMail(fournisseur, entry.getValue()));
            resultats.add("Mail envoyé à " + fournisseur.getNom() + " (" + fournisseur.getEmail() + ")");
        }

        return resultats;
    }

    private String construireCorpsMail(Fournisseur fournisseur, Map<Categorie, List<Medicament>> parCategorie) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour ").append(fournisseur.getNom()).append(",\n\n");
        sb.append("Nous avons besoin d'un devis pour les médicaments suivants :\n\n");

        for (Map.Entry<Categorie, List<Medicament>> entry : parCategorie.entrySet()) {
            sb.append("Catégorie : ").append(entry.getKey().getLibelle()).append("\n");
            for (Medicament m : entry.getValue()) {
                sb.append("  - ").append(m.getNom())
                  .append(" (stock : ").append(m.getUnitesEnStock())
                  .append(", seuil : ").append(m.getNiveauDeReappro()).append(")\n");
            }
            sb.append("\n");
        }

        sb.append("Merci de nous transmettre votre devis.\n\nCordialement,\nLa Pharmacie");
        return sb.toString();
    }

    private void envoyerMail(String email, String nom, String corps) throws IOException {
        Mail mail = new Mail(new Email(fromEmail), "Demande de devis de réapprovisionnement", new Email(email, nom), new Content("text/plain", corps));
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        sg.api(request);
    }

}
