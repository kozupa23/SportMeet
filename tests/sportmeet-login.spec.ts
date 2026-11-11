
import { test, expect } from '@playwright/test';

test.describe('SportMeet - Přihlášení', () => {

    test('SM-LOGIN-01: Přihlašovací formulář', async ({ page }) => {
        await page.goto('http://localhost:8080/login');

        await expect(
            page.getByRole('heading', { name: 'Please sign in' })
        ).toBeVisible();

        await expect(page.getByPlaceholder('Username')).toBeVisible();
        await expect(page.getByPlaceholder('Password')).toBeVisible();
        await expect(
            page.getByRole('button', { name: 'Sign in' })
        ).toBeVisible();
    });

    test('SM-LOGIN-02: Neplatné přihlášení', async ({ page }) => {
        await page.goto('http://localhost:8080/login');

        await page.getByPlaceholder('Username')
            .fill('neexistujici_uzivatel_test');

        await page.getByPlaceholder('Password')
            .fill('nespravne_heslo_test');

        await page.getByRole('button', { name: 'Sign in' }).click();

        await expect(page).toHaveURL(/\/login\?error/);
    });

});
