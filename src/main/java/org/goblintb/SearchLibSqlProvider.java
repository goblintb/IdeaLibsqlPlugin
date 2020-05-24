package org.goblintb;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Ишет строки с ссылками на ресурсы и делает их кликабельными
 *
 */
public class SearchLibSqlProvider extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiLiteralExpression.class),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                                 @NotNull ProcessingContext context) {
                        PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
                        String value = literalExpression.getValue() instanceof String ?
                                (String) literalExpression.getValue() : null;
                        if ((value != null)) {
                            TextRange property = new TextRange(1, value.length() + 1);//без кавычек
                            SimpleReference simpleReference = new SimpleReference(element, property);
                            return new PsiReference[]{simpleReference};
                        }
                        return PsiReference.EMPTY_ARRAY;
                    }
                });
    }
}
