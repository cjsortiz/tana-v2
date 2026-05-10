package com.tana.tana_common.util;

import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.exception.TanaException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    // ✅ cache logo (avoid disk read every loop/send)
    private final FileSystemResource tanaLogo =
        new FileSystemResource("D:/tana-place-images/tana-logo.png");

    @Async
    public void sendHtmlEmail(Map<String, Object> metaData) throws MessagingException, IOException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo((String) metaData.get("to"));
        helper.setSubject((String) metaData.get("from"));

        String category = (String) metaData.get("category");
        String[] colors = getCategoryColors(category);
        MultipartFile[] photos = (MultipartFile[]) metaData.get("photos");

        String htmlContent = build(
            (String) metaData.get("spotName"),
            category,
            colors[0],
            colors[1],
            (String) metaData.get("location"),
            (String) metaData.get("vibe"),
            (String) metaData.get("submitterName"),
            (String) metaData.get("submitterEmail"),
            String.valueOf(photos.length)
        );
        helper.setText(htmlContent, true); // true = HTML

        if (!ObjectUtils.isEmpty(photos)) {
            // ─── Attach photos ───────────────────────────────────────
            for (MultipartFile photo : photos) {
                byte[] bytes = photo.getBytes(); // ✅ force read NOW

                helper.addAttachment(
                    Objects.requireNonNullElse(photo.getOriginalFilename(), "image.jpg"),
                    new org.springframework.core.io.ByteArrayResource(bytes)
                );
            }
        }

        helper.addInline("tanaLogo", tanaLogo);


        mailSender.send(message);
    }


    /**
     * Generates the tana!-branded HTML email sent to the curation team
     * when a new spot is submitted. Design matches the tana! app prototype.
     *
     * Category color mapping (pass the emoji + label as-is from the form):
     *   "🌿 Nature"    → lime green  (#a7c55e / #0c4230)
     *   "🍜 Food"      → orange      (#fe6a36 / #fff)
     *   "🏛️ Culture"  → purple      (#9096ea / #fff)
     *   "☀️ Slow Days" → yellow      (#f7d87d / #654b30)
     *
     * @param spotName        Name of the submitted spot
     * @param category        Category string from the form (emoji + label)
     * @param categoryBg      Category badge background color hex
     * @param categoryColor   Category badge text color hex
     * @param location        Landmark or barangay
     * @param vibe            User's vibe description
     * @param submitterName   Full name of the submitter
     * @param submitterEmail  Email of the submitter
     * @return Full HTML email string
     */
    public String build(
        String spotName,
        String category,
        String categoryBg,
        String categoryColor,
        String location,
        String vibe,
        String submitterName,
        String submitterEmail,
        String photoCount
    ) {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <title>New Spot Submission \u2013 tana!</title>
          <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet"/>
        </head>
        <body style="margin:0; padding:0; background-color:#b0b09e; font-family:'Plus Jakarta Sans', sans-serif;">
 
          <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#b0b09e; padding:40px 20px;">
            <tr>
              <td align="center">
                <table width="560" cellpadding="0" cellspacing="0" style="background-color:#f4f4e8; border-radius:24px; overflow:hidden; box-shadow:0 8px 40px rgba(12,66,48,0.18);">
 
                  <!-- ─── Header ─── -->
                  <tr>
                    <td style="background-color:#0c4230; padding:28px 32px 24px;">
                      <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                          <td>
                            <img src="cid:tanaLogo" width="100" style="display:block;" />
                            <p style="margin:4px 0 0; font-size:10px; font-weight:700; letter-spacing:2px; text-transform:uppercase; color:#a7c55e;">Curator Desk</p>
                          </td>
                          <td align="right" valign="middle">
                            <span style="display:inline-block; background:#a7c55e; color:#0c4230; font-size:10px; font-weight:800; letter-spacing:1.5px; text-transform:uppercase; padding:6px 14px; border-radius:100px;">New Spot &#10022;</span>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
 
                  <!-- Lime strip -->
                  <tr>
                    <td style="background-color:#a7c55e; height:4px; font-size:0; line-height:0;">&nbsp;</td>
                  </tr>
 
                  <!-- ─── Body ─── -->
                  <tr>
                    <td style="padding:32px;">
 
                      <!-- Spot name dark card -->
                      <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#0c4230; border-radius:18px; margin-bottom:20px;">
                        <tr>
                          <td style="padding:24px;">
                            <p style="margin:0 0 6px; font-size:10px; font-weight:700; letter-spacing:2px; text-transform:uppercase; color:#a7c55e;">Submitted Spot</p>
                            <p style="margin:0 0 14px; font-size:28px; font-weight:800; color:#f4f4e8; letter-spacing:-0.4px; line-height:1.1;">%s</p>
                            <span style="display:inline-block; background:%s; color:%s; font-size:10px; font-weight:800; padding:5px 14px; border-radius:100px; letter-spacing:0.5px;">%s</span>
                          </td>
                        </tr>
                      </table>
 
                      <!-- Detail rows white card -->
                      <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#fff; border-radius:16px; border:1px solid rgba(12,66,48,0.09); margin-bottom:20px; border-collapse:collapse; overflow:hidden;">
                        <tr>
                          <td style="padding:16px 20px; border-bottom:1px solid rgba(12,66,48,0.07);">
                            <p style="margin:0 0 3px; font-size:9px; font-weight:700; letter-spacing:2px; text-transform:uppercase; color:#b0b09e;">Location</p>
                            <p style="margin:0; font-size:14px; font-weight:600; color:#0c4230;">%s</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:16px 20px; border-bottom:1px solid rgba(12,66,48,0.07);">
                            <p style="margin:0 0 3px; font-size:9px; font-weight:700; letter-spacing:2px; text-transform:uppercase; color:#b0b09e;">Vibe</p>
                            <p style="margin:0; font-size:14px; font-weight:400; color:#6b6b58; line-height:1.7; font-style:italic;">&ldquo;%s&rdquo;</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:16px 20px;">
                            <p style="margin:0 0 3px; font-size:9px; font-weight:700; letter-spacing:2px; text-transform:uppercase; color:#b0b09e;">Submitted By</p>
                            <p style="margin:0; font-size:14px; font-weight:700; color:#0c4230;">%s</p>
                            <p style="margin:2px 0 0; font-size:13px; font-weight:400; color:#6b6b58;">%s</p>
                          </td>
                        </tr>
                      </table>
 
                      <!-- Photos notice -->
                      <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:rgba(167,197,94,0.12); border:1.5px solid rgba(167,197,94,0.35); border-radius:14px; margin-bottom:24px;">
                        <tr>
                          <td style="padding:14px 18px;">
                            <table cellpadding="0" cellspacing="0">
                              <tr>
                                <td style="font-size:18px; padding-right:12px; vertical-align:top; padding-top:2px;">&#128247;</td>
                                <td>
                                  <p style="margin:0 0 2px; font-size:12px; font-weight:700; color:#3f7005;">Photos Attached</p>
                                  <p style="margin:0; font-size:12px; font-weight:400; color:#6b6b58; line-height:1.6;">Up to %s photos submitted. Review before publishing.</p>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                      </table>
 
                      <!-- CTA buttons -->
                      <table cellpadding="0" cellspacing="0" width="100%%">
                        <tr>
                          <td style="padding-right:10px; width:65%%;">
                            <a href="#" style="display:block; background-color:#0c4230; color:#a7c55e; text-decoration:none; font-size:13px; font-weight:800; letter-spacing:0.5px; padding:16px 24px; border-radius:100px; text-align:center;">
                              Review Submission &#8599;
                            </a>
                          </td>
                          <td>
                            <a href="#" style="display:block; background-color:#fff; color:#6b6b58; text-decoration:none; font-size:13px; font-weight:700; padding:16px 24px; border-radius:100px; border:1.5px solid rgba(12,66,48,0.14); text-align:center;">
                              Decline
                            </a>
                          </td>
                        </tr>
                      </table>
 
                    </td>
                  </tr>
 
                  <!-- ─── Footer ─── -->
                  <tr>
                    <td style="background-color:#0a3526; padding:18px 32px; border-top:1px solid rgba(167,197,94,0.12);">
                      <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                          <td><p style="margin:0; font-size:11px; font-weight:500; color:rgba(244,244,232,0.3);">Automated &middot; Do not reply</p></td>
                          <td align="right"><p style="margin:0; font-size:16px; font-weight:800; color:rgba(244,244,232,0.3); letter-spacing:-0.3px;">tana!</p></td>
                        </tr>
                      </table>
                    </td>
                  </tr>
 
                </table>
              </td>
            </tr>
          </table>
 
        </body>
        </html>
        """.formatted(
            spotName,
            categoryBg, categoryColor, category,
            location,
            vibe,
            submitterName, submitterEmail,photoCount
        );
    }

    /**
     * Generates the tana!-branded HTML confirmation email sent to the user
     * after they submit a spot. Design matches the tana! app prototype.
     *
     * Category color mapping:
     *   "🌿 Nature"    → #a7c55e bg / #0c4230 text
     *   "🍜 Food"      → #fe6a36 bg / #fff text
     *   "🏛️ Culture"  → #9096ea bg / #fff text
     *   "☀️ Slow Days" → #f7d87d bg / #654b30 text
     *
     * @param spotName       Name of the submitted spot
     * @param category       Category string from the form (emoji + label)
     * @param categoryBg     Category badge background color hex
     * @param categoryColor  Category badge text color hex
     * @param location       Landmark or barangay
     * @param vibe           User's vibe description
     * @param submitterName  Full name of the submitter (first name extracted for greeting)
     * @return Full HTML email string
     */
    public static String build(
        String spotName,
        String category,
        String categoryBg,
        String categoryColor,
        String location,
        String vibe,
        String submitterName
    ) {
        String firstName = submitterName.contains(" ")
            ? submitterName.substring(0, submitterName.indexOf(" "))
            : submitterName;

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <title>Your spot is in good hands \u2013 tana!</title>
          <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet"/>
        </head>
        <body style="margin:0; padding:0; background-color:#b0b09e; font-family:'Plus Jakarta Sans', sans-serif;">
 
          <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#b0b09e; padding:40px 20px;">
            <tr>
              <td align="center">
                <table width="560" cellpadding="0" cellspacing="0" style="background-color:#f4f4e8; border-radius:24px; overflow:hidden; box-shadow:0 8px 40px rgba(12,66,48,0.18);">
 
                  <!-- ─── Header ─── -->
                  <tr>
                    <td style="background-color:#0c4230; padding:28px 32px 24px;">
                      <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                          <td>
                          <img src="cid:tanaLogo" width="100" style="display:block;" />
                          <p style="margin:4px 0 0; font-size:10px; font-weight:700; letter-spacing:2px; text-transform:uppercase; color:#a7c55e;">Spot Received</p>
                          </td>
                          <td align="right" valign="middle">
                            <span style="display:inline-block; background:rgba(167,197,94,0.18); color:#a7c55e; font-size:10px; font-weight:700; letter-spacing:1px; text-transform:uppercase; padding:6px 14px; border-radius:100px; border:1.5px solid rgba(167,197,94,0.35);">&#10003; Submitted</span>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
 
                  <!-- Lime strip -->
                  <tr>
                    <td style="background-color:#a7c55e; height:4px; font-size:0; line-height:0;">&nbsp;</td>
                  </tr>
 
                  <!-- ─── Greeting ─── -->
                  <tr>
                    <td style="padding:32px 32px 0;">
                      <p style="margin:0 0 6px; font-size:11px; font-weight:700; letter-spacing:2px; text-transform:uppercase; color:#a7c55e;">Hey, %s.</p>
                      <h1 style="margin:0 0 14px; font-size:30px; font-weight:800; color:#0c4230; letter-spacing:-0.5px; line-height:1.15;">Your spot is<br/>in good hands.</h1>
                      <p style="margin:0; font-size:14px; font-weight:400; color:#6b6b58; line-height:1.75;">
                        We got your submission for <strong style="color:#0c4230; font-weight:700;">%s</strong>. Every spot on tana! gets a personal review before it goes live.
                      </p>
                    </td>
                  </tr>
 
                  <!-- ─── Spot summary dark card ─── -->
                  <tr>
                    <td style="padding:24px 32px 0;">
                      <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#0c4230; border-radius:18px;">
                        <tr>
                          <td style="padding:22px;">
                            <p style="margin:0 0 4px; font-size:9px; font-weight:700; letter-spacing:2px; text-transform:uppercase; color:rgba(167,197,94,0.7);">Your Submission</p>
                            <p style="margin:0 0 12px; font-size:22px; font-weight:800; color:#f4f4e8; letter-spacing:-0.3px; line-height:1.15;">%s</p>
                            <!-- Category + Location pills -->
                            <table cellpadding="0" cellspacing="0" style="margin-bottom:14px;">
                              <tr>
                                <td style="padding-right:8px;">
                                  <span style="display:inline-block; background:%s; color:%s; font-size:10px; font-weight:800; padding:4px 12px; border-radius:100px;">%s</span>
                                </td>
                                <td>
                                  <span style="display:inline-block; background:rgba(244,244,232,0.10); color:rgba(244,244,232,0.6); font-size:10px; font-weight:600; padding:4px 12px; border-radius:100px;">&#128205; %s</span>
                                </td>
                              </tr>
                            </table>
                            <!-- Vibe -->
                            <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:rgba(244,244,232,0.07); border-radius:12px;">
                              <tr>
                                <td style="padding:14px;">
                                  <p style="margin:0; font-size:13px; font-weight:400; color:rgba(244,244,232,0.7); line-height:1.7; font-style:italic;">&ldquo;%s&rdquo;</p>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
 
                  <!-- ─── What Happens Next ─── -->
                  <tr>
                    <td style="padding:28px 32px 0;">
                      <p style="margin:0 0 18px; font-size:9px; font-weight:700; letter-spacing:2px; text-transform:uppercase; color:#b0b09e;">What Happens Next</p>
 
                      <!-- Step 1 -->
                      <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:12px;">
                        <tr>
                          <td style="width:32px; vertical-align:top; padding-top:0; padding-right:14px;">
                            <div style="width:32px; height:32px; background:#0c4230; border-radius:50%%; text-align:center; line-height:32px; font-size:13px; font-weight:800; color:#a7c55e;">1</div>
                          </td>
                          <td style="background:#fff; border-radius:14px; padding:14px 16px; border:1px solid rgba(12,66,48,0.08);">
                            <p style="margin:0 0 3px; font-size:13px; font-weight:700; color:#0c4230;">Curator Review</p>
                            <p style="margin:0; font-size:12px; font-weight:400; color:#6b6b58; line-height:1.6;">A real person goes through your spot &mdash; location, vibe, and photos.</p>
                          </td>
                        </tr>
                      </table>
 
                      <!-- Step 2 -->
                      <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:12px;">
                        <tr>
                          <td style="width:32px; vertical-align:top; padding-top:0; padding-right:14px;">
                            <div style="width:32px; height:32px; background:#0c4230; border-radius:50%%; text-align:center; line-height:32px; font-size:13px; font-weight:800; color:#a7c55e;">2</div>
                          </td>
                          <td style="background:#fff; border-radius:14px; padding:14px 16px; border:1px solid rgba(12,66,48,0.08);">
                            <p style="margin:0 0 3px; font-size:13px; font-weight:700; color:#0c4230;">We Might Reach Out</p>
                            <p style="margin:0; font-size:12px; font-weight:400; color:#6b6b58; line-height:1.6;">If we need more context or better photos, we&rsquo;ll come back to you directly.</p>
                          </td>
                        </tr>
                      </table>
 
                      <!-- Step 3 -->
                      <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                          <td style="width:32px; vertical-align:top; padding-top:0; padding-right:14px;">
                            <div style="width:32px; height:32px; background:#a7c55e; border-radius:50%%; text-align:center; line-height:32px; font-size:13px; font-weight:800; color:#0c4230;">3</div>
                          </td>
                          <td style="background:#fff; border-radius:14px; padding:14px 16px; border:1px solid rgba(12,66,48,0.08);">
                            <p style="margin:0 0 3px; font-size:13px; font-weight:700; color:#0c4230;">It Goes Live &#10022;</p>
                            <p style="margin:0; font-size:12px; font-weight:400; color:#6b6b58; line-height:1.6;">Approved spots join tana! &mdash; discoverable by everyone exploring the city.</p>
                          </td>
                        </tr>
                      </table>
 
                    </td>
                  </tr>
 
                  <!-- ─── Closing note ─── -->
                  <tr>
                    <td style="padding:24px 32px 32px;">
                      <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#0c4230; border-radius:18px;">
                        <tr>
                          <td style="padding:22px 24px;">
                            <p style="margin:0 0 12px; font-size:14px; font-weight:400; color:rgba(244,244,232,0.75); line-height:1.75; font-style:italic;">
                              &ldquo;The best spots are usually the ones people almost kept to themselves. Thanks for sharing yours.&rdquo;
                            </p>
                            <p style="margin:0; font-size:10px; font-weight:700; letter-spacing:2px; text-transform:uppercase; color:#a7c55e;">&mdash; The tana! Curators</p>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
 
                  <!-- ─── Footer ─── -->
                  <tr>
                    <td style="background-color:#0a3526; padding:18px 32px; border-top:1px solid rgba(167,197,94,0.12);">
                      <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr>
                          <td><p style="margin:0; font-size:11px; font-weight:500; color:rgba(244,244,232,0.3);">Questions? <a href="mailto:hello@tana.com" style="color:rgba(167,197,94,0.5); text-decoration:none;">hello@tana.com</a></p></td>
                          <td align="right"><p style="margin:0; font-size:16px; font-weight:800; color:rgba(244,244,232,0.3); letter-spacing:-0.3px;">tana!</p></td>
                        </tr>
                      </table>
                    </td>
                  </tr>
 
                </table>
              </td>
            </tr>
          </table>
 
        </body>
        </html>
        """.formatted(
            firstName, spotName,
            spotName,
            categoryBg, categoryColor, category,
            location,
            vibe
        );
    }

    // --- Category color helpers ---
    public static String[] getCategoryColors(String category) {
        if (category.contains("Nature"))    return new String[]{"#a7c55e", "#0c4230"};
        if (category.contains("Food"))      return new String[]{"#fe6a36", "#ffffff"};
        if (category.contains("Culture"))   return new String[]{"#9096ea", "#ffffff"};
        if (category.contains("Slow Days")) return new String[]{"#f7d87d", "#654b30"};
        return new String[]{"#b0b09e", "#0c4230"}; // fallback
    }
}
