import { WebPlugin } from '@capacitor/core';

import type { IntentUriPlugin } from './definitions';

export class IntentUriWeb extends WebPlugin implements IntentUriPlugin {
  async openUri(options: { url: string }): Promise<{ completed: true } | { completed: false, message: string }> {
    window.open(options.url, '_blank');
    return { completed: true };
  }
}
